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
    buttons.append(button("Support on Ko-fi", load_links().get("kofi") or KOFI_URL, "EMOJI_KOFI", "☕"))

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


LINKS_FILE = Path(__file__).resolve().parent / "mod_links.json"
FOLDERS_FILE = LINKS_FILE.parents[2] / ".media" / "tools" / "mod_folders.json"
MODMENU_KEYS = ("modmenu.discord", "modmenu.kofi", "modmenu.wiki", "modmenu.modrinth", "modmenu.curseforge")
CONTACT_KEYS = ("homepage", "sources", "issues")


def load_links():
    return json.loads(LINKS_FILE.read_text(encoding="utf-8"))


def mod_links(config, mod):
    github = f"https://github.com/{mod['github']}"
    modrinth = f"https://modrinth.com/mod/{mod['modrinth']}" if mod.get("modrinth") else ""
    return {
        "homepage": modrinth or github,
        "sources": github,
        "issues": f"{github}/issues",
        "wiki": f"{github}/wiki" if mod.get("wiki") else "",
        "discord": config["discord"],
        "kofi": config["kofi"],
        "modrinth": modrinth,
        "curseforge": f"https://www.curseforge.com/minecraft/mc-mods/{mod['curseforge']}" if mod.get("curseforge") else "",
    }


def links_for_repo(root):
    config = load_links()
    mod = config["mods"].get(fabric_mod_json(root).get("id", ""))
    return mod_links(config, mod) if mod else None


def json_spans(text):
    spans = {}

    def skip(i):
        while text[i] in " \t\r\n":
            i += 1
        return i

    def string_end(i):
        i += 1
        while text[i] != '"':
            i += 2 if text[i] == "\\" else 1
        return i + 1

    def value(i, path):
        i = skip(i)
        start = i
        if text[i] == "{":
            i = skip(i + 1)
            while text[i] != "}":
                key_start = i
                key_end = string_end(i)
                key = json.loads(text[key_start:key_end])
                i = skip(key_end) + 1
                value_start, i = value(i, path + (key,))
                spans[path + (key,)] = (key_start, value_start, i)
                i = skip(i)
                if text[i] == ",":
                    i = skip(i + 1)
            return start, i + 1
        if text[i] == "[":
            i = skip(i + 1)
            index = 0
            while text[i] != "]":
                _, i = value(i, path + (index,))
                index += 1
                i = skip(i)
                if text[i] == ",":
                    i = skip(i + 1)
            return start, i + 1
        if text[i] == '"':
            return start, string_end(i)
        while text[i] not in ",}] \t\r\n":
            i += 1
        return start, i

    spans[()] = (0,) + value(0, ())
    return spans


def render_json(obj, indent, unit):
    if not isinstance(obj, dict):
        return json.dumps(obj, ensure_ascii=False)
    if not obj:
        return "{}"
    inner = indent + unit
    items = ",\n".join(f"{inner}{json.dumps(k, ensure_ascii=False)}: {render_json(v, inner, unit)}" for k, v in obj.items())
    return "{\n" + items + "\n" + indent + "}"


def line_indent(text, position):
    line_start = text.rfind("\n", 0, position) + 1
    return re.match(r"[ \t]*", text[line_start:]).group(0)


def set_json_object(text, path, obj, unit, after=("authors", "description", "name")):
    spans = json_spans(text)
    if path in spans:
        key_start, value_start, value_end = spans[path]
        return text[:value_start] + render_json(obj, line_indent(text, key_start), unit) + text[value_end:]
    parent = path[:-1]
    _, parent_start, parent_end = spans[parent]
    siblings = [spans[p] for p in spans if len(p) == len(path) and p[:-1] == parent]
    indent = line_indent(text, siblings[0][0]) if siblings else line_indent(text, parent_start) + unit
    entry = f'{json.dumps(path[-1])}: {render_json(obj, indent, unit)}'
    anchor = next((spans[parent + (key,)] for key in after if parent + (key,) in spans), None) if not parent else None
    if anchor:
        return text[:anchor[2]] + f",\n{indent}{entry}" + text[anchor[2]:]
    if siblings:
        last_end = max(s[2] for s in siblings)
        return text[:last_end] + f",\n{indent}{entry}" + text[last_end:]
    close = parent_end - 1
    return text[:parent_start + 1] + f"\n{indent}{entry}\n{line_indent(text, parent_start)}" + text[close:]


def update_fabric_mod_json(text, links):
    newline = "\r\n" if "\r\n" in text else "\n"
    text = text.replace("\r\n", "\n")
    data = json.loads(text)
    unit = re.search(r"\n([ \t]+)\S", text).group(1)
    contact = {k: links[k] for k in CONTACT_KEYS}
    contact.update({k: v for k, v in (data.get("contact") or {}).items() if k not in CONTACT_KEYS})
    text = set_json_object(text, ("contact",), contact, unit)
    wanted = {"modmenu.discord": links["discord"], "modmenu.kofi": links["kofi"], "modmenu.wiki": links["wiki"],
              "modmenu.modrinth": links["modrinth"], "modmenu.curseforge": links["curseforge"]}
    old = ((data.get("custom") or {}).get("modmenu") or {}).get("links") or {}
    modmenu_links = {k: v for k, v in wanted.items() if v}
    modmenu_links.update({k: v for k, v in old.items() if k not in MODMENU_KEYS})
    data = json.loads(text)
    if "custom" not in data:
        text = set_json_object(text, ("custom",), {"modmenu": {"links": modmenu_links}}, unit, after=())
    elif "modmenu" not in data["custom"]:
        text = set_json_object(text, ("custom", "modmenu"), {"links": modmenu_links}, unit)
    else:
        text = set_json_object(text, ("custom", "modmenu", "links"), modmenu_links, unit)
    json.loads(text)
    return text.replace("\n", newline)


def update_mods_toml(text, links):
    newline = "\r\n" if "\r\n" in text else "\n"
    lines = text.replace("\r\n", "\n").split("\n")
    issue = f'issueTrackerURL = "{links["issues"]}"'
    found = [i for i, line in enumerate(lines) if re.match(r"\s*#?\s*issueTrackerURL\s*=", line) and not any(l.startswith("[") for l in lines[:i])]
    if found:
        lines[found[0]] = issue
    else:
        anchor = next((i for i, line in enumerate(lines) if re.match(r"\s*(license|loaderVersion)\s*=", line)), 0)
        lines.insert(anchor + 1, issue)
    start = next((i for i, line in enumerate(lines) if line.strip() == "[[mods]]"), None)
    if start is not None:
        end = next((i for i in range(start + 1, len(lines)) if lines[i].startswith("[")), len(lines))
        display = f'displayURL = "{links["homepage"]}"'
        found = [i for i in range(start + 1, end) if re.match(r"\s*#?\s*displayURL\s*=", lines[i])]
        if found:
            lines[found[0]] = display
        else:
            anchor = next((i for i in range(start + 1, end) if re.match(r"\s*displayName\s*=", lines[i])), start)
            lines.insert(anchor + 1, display)
    return newline.join(lines)


def metadata_files(folder):
    fabric = [folder / "fabric/src/main/resources/fabric.mod.json", folder / "src/main/resources/fabric.mod.json"]
    neoforge = [folder / "neoforge/src/main/resources/META-INF/neoforge.mods.toml"]
    return [(p, update_fabric_mod_json) for p in fabric if p.exists()][:1] + [(p, update_mods_toml) for p in neoforge if p.exists()]


def modrinth_links_payload(links):
    return {
        "issues_url": links["issues"],
        "source_url": links["sources"],
        "wiki_url": links["wiki"] or None,
        "discord_url": links["discord"],
        "donation_urls": [{"id": "ko-fi", "platform": "Ko-fi", "url": links["kofi"]}],
    }


def selected_mods(config, only):
    mods = config["mods"]
    if not only:
        return mods
    chosen = {k: v for k, v in mods.items() if only.lower() in (k.lower(), v["name"].lower())}
    if not chosen:
        raise SystemExit(f"No mod '{only}' in {LINKS_FILE.name} (use the mod id or name)")
    return chosen


def cmd_links(root, args):
    config = load_links()
    if not FOLDERS_FILE.exists():
        raise SystemExit(f"{FOLDERS_FILE} is missing: it maps every mod id to its folder on this PC (local only, not in git)")
    folders = json.loads(FOLDERS_FILE.read_text(encoding="utf-8"))
    base = Path(args.root) if args.root else LINKS_FILE.parents[4]
    changed = 0
    for mod_id, mod in selected_mods(config, args.only).items():
        if mod_id not in folders:
            print(f"[skip] {mod['name']}: no folder for '{mod_id}' in {FOLDERS_FILE.name}")
            continue
        folder = base / folders[mod_id]
        if not folder.is_dir():
            print(f"[skip] {mod['name']}: folder not found ({folder})")
            continue
        links = mod_links(config, mod)
        for path, update in metadata_files(folder):
            old = path.read_bytes().decode("utf-8")
            new = update(old, links)
            rel = path.relative_to(folder).as_posix()
            if new == old:
                print(f"[ok]      {mod['name']}: {rel}")
                continue
            changed += 1
            if not args.check:
                path.write_bytes(new.encode("utf-8"))
            print(f"[{'would' if args.check else 'updated'}] {mod['name']}: {rel}")
    print(f"\n{changed} file(s) {'need an update' if args.check else 'updated'}")


def cmd_modrinth_links(root, args):
    config = load_links()
    token = env("MODRINTH_API_KEY") or env("MODRINTH_TOKEN")
    if not args.check and not token:
        raise SystemExit("Set $env:MODRINTH_TOKEN (Modrinth PAT with Write projects) first")
    for mod in selected_mods(config, args.only).values():
        if not mod.get("modrinth"):
            print(f"[skip] {mod['name']}: no Modrinth project")
            continue
        payload = modrinth_links_payload(mod_links(config, mod))
        if args.check:
            print(f"[check] {mod['name']} ({mod['modrinth']}): {json.dumps(payload, ensure_ascii=False)}")
            continue
        status = post_json(f"{env('MODRINTH_API', 'https://api.modrinth.com/v2')}/project/{mod['modrinth']}", payload, "PATCH", {"Authorization": token})
        print(f"[updated] {mod['name']} ({mod['modrinth']}): HTTP {status}")


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
    links = links_for_repo(root)
    payload = {"body": body, **(modrinth_links_payload(links) if links else {})}
    status = post_json(f"{env('MODRINTH_API', 'https://api.modrinth.com/v2')}/project/{project}", payload, "PATCH", {"Authorization": token})
    print(f"Modrinth description updated ({len(body)} characters{', links too' if links else ''}, HTTP {status})")


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
    links = sub.add_parser("links", help=f"write the links from {LINKS_FILE.name} into fabric.mod.json and neoforge.mods.toml of every mod")
    links.add_argument("--check", action="store_true", help="only list what would change")
    links.add_argument("--only", default="", help="one mod (mod id or name)")
    links.add_argument("--root", default="", help="folder that contains all mod folders")
    links.set_defaults(run=cmd_links)
    mr_links = sub.add_parser("modrinth-links", help=f"set the project links of every Modrinth project from {LINKS_FILE.name}")
    mr_links.add_argument("--check", action="store_true", help="only print what would be sent")
    mr_links.add_argument("--only", default="", help="one mod (mod id or name)")
    mr_links.set_defaults(run=cmd_modrinth_links)
    args = parser.parse_args()
    args.run(Path.cwd(), args)


if __name__ == "__main__":
    main()
