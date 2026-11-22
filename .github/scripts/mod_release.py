import argparse
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

DISCORD_DESCRIPTION_LIMIT = 4096
MODRINTH_BODY_LIMIT = 65536
EMBED_COLOR = 8421504
KOFI_URL = "https://ko-fi.com/fichteee"
USER_AGENT = "mod-release-workflow/1.0"
MAX_DISCORD_IMAGES = 4
LINK_PREFIX = re.compile(r"(?i)^(https?:|mailto:|#|data:)")
MARKDOWN_IMAGE = re.compile(r"!\[[^\]]*\]\(([^)\s]+)\)")
HTML_IMAGE = re.compile(r'<img\b[^>]*\bsrc="([^"]+)"[^>]*>', re.IGNORECASE)


def env(name, default=""):
    return os.environ.get(name, "").strip() or default


def read_property(text, *keys):
    for key in keys:
        match = re.search(rf"^{re.escape(key)}\s*=\s*(.+?)\s*$", text, re.MULTILINE)
        if match:
            return match.group(1)
    return ""


def newest_changelog_section(root):
    path = root / "CHANGELOG.md"
    if not path.exists():
        return ""
    text = path.read_text(encoding="utf-8")
    releases = [s for s in re.split(r"(?m)^(?=## \[)", text) if s.startswith("## [") and not s.startswith("## [Unreleased]")]
    if releases:
        return re.sub(r"(?m)^\[[^\]]+\]: .*$", "", releases[0]).strip()
    legacy = [s for s in re.split(r"(?m)^(?=# )", text) if s.strip().startswith("#")]
    return legacy[0].strip() if legacy else ""


def absolute_url(path):
    repository = env("REPOSITORY")
    if not repository or LINK_PREFIX.match(path):
        return path
    clean = path[2:] if path.startswith("./") else path.lstrip("/")
    image = re.search(r"(?i)\.(png|jpe?g|gif|webp|svg)$", clean)
    ref = env("COMMIT_SHA", "HEAD")
    host = f"raw.githubusercontent.com/{repository}/{ref}" if image else f"github.com/{repository}/blob/{ref}"
    return f"https://{host}/{clean}"


def absolute_links(text):
    text = re.sub(r'((?:src|href)=")([^"]+)"', lambda m: f'{m.group(1)}{absolute_url(m.group(2))}"', text)
    return re.sub(r"(\]\()([^)\s]+)\)", lambda m: f"{m.group(1)}{absolute_url(m.group(2))})", text)


def changelog_images(section):
    matches = sorted([*MARKDOWN_IMAGE.finditer(section), *HTML_IMAGE.finditer(section)], key=lambda m: m.start())
    images = []
    for match in matches:
        url = absolute_url(match.group(1))
        if url.startswith("http") and url not in images:
            images.append(url)
    return images


def without_images(section):
    text = HTML_IMAGE.sub("", MARKDOWN_IMAGE.sub("", section))
    lines = [line for line in text.splitlines() if line.strip() not in ("-", "*")]
    return re.sub(r"\n{3,}", "\n\n", "\n".join(lines)).strip()


def fabric_mod_json(root):
    path = root / "fabric" / "src" / "main" / "resources" / "fabric.mod.json"
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return {}


def mod_name(root):
    return fabric_mod_json(root).get("name") or env("REPO_NAME").replace("-", " ")


def mod_icon(root):
    if env("ICON_URL"):
        return env("ICON_URL")
    icon = fabric_mod_json(root).get("icon")
    if isinstance(icon, dict):
        icon = icon[max(icon, key=lambda size: int(size) if str(size).isdigit() else 0)]
    for folder in ("common", "fabric"):
        candidate = f"{folder}/src/main/resources/{icon or 'icon.png'}"
        if (root / candidate).exists():
            return absolute_url(candidate)
    return ""


def emoji(value):
    if ":" not in value:
        return {}
    name, emoji_id = value.split(":", 1)
    return {"emoji": {"name": name, "id": emoji_id}}


def button(label, url, emoji_var, fallback=None):
    result = {"type": 2, "style": 5, "label": label, "url": url}
    result.update(emoji(env(emoji_var)))
    if "emoji" not in result and fallback:
        result["emoji"] = {"name": fallback}
    return result


def post_json(url, payload, method="POST", headers=None):
    request = urllib.request.Request(
        url,
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json", "User-Agent": USER_AGENT, **(headers or {})},
        method=method,
    )
    try:
        with urllib.request.urlopen(request) as response:
            return response.status
    except urllib.error.HTTPError as error:
        raise SystemExit(f"{method} failed with HTTP {error.code}: {error.read().decode('utf-8', 'replace')[:500]}")


def discord_payload(root):
    properties = (root / "gradle.properties").read_text(encoding="utf-8")
    version = read_property(properties, "mod_version", "version") or "unknown"
    minecraft = read_property(properties, "minecraft_version")
    built = json.loads(env("LOADERS_JSON", "[]")) or [l for l in ("fabric", "neoforge") if (root / l).is_dir()]
    loaders = " · ".join("NeoForge" if l == "neoforge" else "Fabric" for l in built)
    release_type = env("RELEASE_TYPE", "release").capitalize()

    to_modrinth = env("TO_MODRINTH") == "true" and env("MR_ID")
    to_curseforge = env("TO_CURSEFORGE") == "true" and env("CF_ID")
    to_github = env("TO_GITHUB") == "true"

    section = newest_changelog_section(root)
    images = changelog_images(section) or [absolute_url(url.strip()) for url in env("IMAGE_URL").split(",") if url.strip()]
    images = images[:MAX_DISCORD_IMAGES]
    changelog = env("CHANGELOG") or without_images(section) or "No changelog provided"
    changelog = changelog.replace("```", "`​``")
    room = DISCORD_DESCRIPTION_LIMIT - len("**Changelog:**\n```\n\n```")
    if len(changelog) > room:
        changelog = changelog[: room - 2].rstrip() + "\n…"

    sources = [name for name, on in (("modrinth.com", to_modrinth), ("curseforge.com", to_curseforge), ("github.com", to_github)) if on]
    icon = mod_icon(root)

    fields = [{"name": "Version", "value": version, "inline": True}]
    if minecraft:
        fields.append({"name": "Minecraft", "value": minecraft, "inline": True})
    fields += [
        {"name": "Loader", "value": loaders or "-", "inline": True},
        {"name": "Release Type", "value": release_type, "inline": True},
        {"name": "Date Published", "value": f"<t:{int(time.time())}:F>", "inline": True},
    ]

    buttons = []
    if to_modrinth:
        buttons.append(button("View on Modrinth", f"https://modrinth.com/mod/{env('MR_ID')}", "EMOJI_MODRINTH"))
    if to_curseforge:
        buttons.append(button("View on CurseForge", f"https://www.curseforge.com/projects/{env('CF_ID')}", "EMOJI_CURSEFORGE"))
    if to_github:
        buttons.append(button("View on GitHub", f"https://github.com/{env('REPOSITORY')}/releases", "EMOJI_GITHUB"))
    buttons.append(button("Support on Ko-fi", KOFI_URL, "EMOJI_KOFI", "☕"))

    page = f"https://modrinth.com/mod/{env('MR_ID')}" if to_modrinth else f"https://github.com/{env('REPOSITORY')}/releases"
    author = {"name": "From " + " & ".join(sources) if sources else "Mod Update"}
    embed = {
        "author": author,
        "title": f"{mod_name(root)} has been updated",
        "url": page,
        "color": EMBED_COLOR,
        "description": f"**Changelog:**\n```\n{changelog}\n```",
        "fields": fields,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }
    if icon:
        author["icon_url"] = icon
        embed["thumbnail"] = {"url": icon}
    if images:
        embed["image"] = {"url": images[0]}
    gallery = [{"url": page, "image": {"url": image}} for image in images[1:]]

    role = env("ROLE_ID")
    return {
        "content": f"<@&{role}>" if role else "",
        "allowed_mentions": {"parse": [], "roles": [role] if role else []},
        "embeds": [embed, *gallery],
        "components": [{"type": 1, "components": buttons}],
    }


def modrinth_body(root):
    readme = root / "README.md"
    if not readme.exists():
        raise SystemExit("README.md not found, it is the Modrinth description")
    text = absolute_links(readme.read_text(encoding="utf-8"))
    if len(text) > MODRINTH_BODY_LIMIT:
        raise SystemExit(f"README.md has {len(text)} characters, Modrinth allows {MODRINTH_BODY_LIMIT}. Move sections into the wiki.")
    return text


def cmd_changelog(root, args):
    print(absolute_links(newest_changelog_section(root)) or "No changelog available")


def cmd_discord(root, args):
    payload = discord_payload(root)
    if args.dry_run:
        print(json.dumps(payload, indent=2, ensure_ascii=False))
        return
    webhook = env("WEBHOOK")
    if not webhook:
        print("::warning::DISCORD_WEBHOOK secret is not set, no Discord message sent.")
        return
    print(f"Discord answered {post_json(webhook + '?with_components=true', payload)}")


def cmd_modrinth_description(root, args):
    body = modrinth_body(root)
    if args.check:
        print(f"README.md is ready for Modrinth ({len(body)} characters)")
        return
    project, token = env("MR_ID"), env("MODRINTH_API_KEY")
    if not project or not token:
        raise SystemExit("MODRINTH_PROJECT_ID variable or MODRINTH_API_KEY secret is missing")
    status = post_json(f"{env('MODRINTH_API', 'https://api.modrinth.com/v2')}/project/{project}", {"body": body}, "PATCH", {"Authorization": token})
    print(f"Modrinth description updated ({len(body)} characters, HTTP {status})")


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser(description="Release helpers shared by all mods (run from the mod repo root).")
    sub = parser.add_subparsers(dest="command", required=True)
    sub.add_parser("changelog", help="print the newest CHANGELOG.md section").set_defaults(run=cmd_changelog)
    discord = sub.add_parser("discord", help="post the update message to the Discord webhook")
    discord.add_argument("--dry-run", action="store_true", help="print the message instead of sending it")
    discord.set_defaults(run=cmd_discord)
    modrinth = sub.add_parser("modrinth-description", help="upload README.md as the Modrinth description")
    modrinth.add_argument("--check", action="store_true", help="only build and check the text, upload nothing")
    modrinth.set_defaults(run=cmd_modrinth_description)
    args = parser.parse_args()
    args.run(Path.cwd(), args)


if __name__ == "__main__":
    main()
