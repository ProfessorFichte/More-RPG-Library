package com.mrpg_lib.client.animation;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class AnimationBlender {
    public static final float FADE_IN_TICKS = 5.0F;
    public static final float FADE_OUT_TICKS = 5.0F;
    private static final float LOOP_TOLERANCE_TICKS = 2.0F;
    private static final float MAX_FRAME_GAP_TICKS = 40.0F;
    private static final Vector3f SCRATCH = new Vector3f();

    private final Map<Entity, Session> sessions = new WeakHashMap<>();

    public static int lengthTicks(Animation animation) {
        return MathHelper.ceil(animation.lengthInSeconds() * 20.0F);
    }

    public static void movement(SinglePartEntityModel<?> model, Animation animation, float limbSwing, float limbSwingAmount, float speed, float amount, float scale) {
        float intensity = Math.min(limbSwingAmount * amount, 1.0F) * scale;
        if (intensity > 0.0F) {
            AnimationHelper.animate(model, animation, (long) (limbSwing * 50.0F * speed), intensity, SCRATCH);
        }
    }

    public Session begin(Entity entity, float ageInTicks) {
        Session session = sessions.computeIfAbsent(entity, e -> new Session());
        session.beginFrame(ageInTicks);
        return session;
    }

    public static final class Session {
        private final List<Channel> channels = new ArrayList<>(4);
        private float age;
        private float delta;
        private boolean started;

        private void beginFrame(float ageInTicks) {
            if (!started) {
                delta = 0.0F;
                started = true;
            } else {
                delta = ageInTicks - age;
                if (delta < 0.0F || delta > MAX_FRAME_GAP_TICKS) {
                    delta = 0.0F;
                }
            }
            age = ageInTicks;
        }

        public void action(AnimationState state, Animation animation) {
            observe(state, animation, false);
        }

        public void hold(AnimationState state, Animation animation) {
            observe(state, animation, true);
        }

        private void observe(AnimationState state, Animation animation, boolean hold) {
            Channel channel = channel(animation);
            boolean running = state.isRunning();
            if (running && !channel.running) {
                channel.pending = true;
            }
            if (!running && channel.running && channel.playing) {
                channel.stopAge = age;
            }
            if (!running && !channel.playing) {
                channel.pending = false;
            }
            channel.running = running;
            channel.hold = hold;
        }

        private Channel channel(Animation animation) {
            for (Channel channel : channels) {
                if (channel.animation == animation) {
                    return channel;
                }
            }
            Channel channel = new Channel(animation);
            channels.add(channel);
            return channel;
        }

        public float apply(SinglePartEntityModel<?> model, Animation idle) {
            resolve();

            float strongest = 0.0F;
            for (Channel channel : channels) {
                float target = channel.playing ? 1.0F : 0.0F;
                float step = delta / (channel.playing ? FADE_IN_TICKS : FADE_OUT_TICKS);
                channel.weight = channel.playing
                        ? Math.min(1.0F, channel.weight + step)
                        : Math.max(0.0F, channel.weight - step);
                if (channel.weight == 0.0F && target == 0.0F) {
                    continue;
                }
                float eased = channel.weight * channel.weight * (3.0F - 2.0F * channel.weight);
                strongest = Math.max(strongest, eased);
                float elapsed = Math.max(0.0F, age - channel.startAge);
                AnimationHelper.animate(model, channel.animation, (long) (elapsed * 50.0F), eased, SCRATCH);
            }

            float idleScale = 1.0F - strongest;
            if (idle != null && idleScale > 0.0F) {
                AnimationHelper.animate(model, idle, (long) ((double) age * 50.0), idleScale, SCRATCH);
            }
            return strongest;
        }

        private void resolve() {
            boolean anyPlaying = false;
            for (Channel channel : channels) {
                if (channel.playing && isFinished(channel)) {
                    channel.playing = false;
                }
                anyPlaying |= channel.playing;
            }

            for (Channel channel : channels) {
                if (channel.pending && channel.hold) {
                    for (Channel other : channels) {
                        other.playing = false;
                    }
                    start(channel);
                    return;
                }
            }

            if (!anyPlaying) {
                for (Channel channel : channels) {
                    if (channel.pending) {
                        start(channel);
                        return;
                    }
                }
            }
        }

        private void start(Channel channel) {
            channel.playing = true;
            channel.pending = false;
            channel.startAge = age;
            channel.stopAge = age;
        }

        private boolean isFinished(Channel channel) {
            float elapsed = age - channel.startAge;
            float length = channel.animation.lengthInSeconds() * 20.0F;
            if (channel.hold && channel.running) {
                return false;
            }
            if (channel.animation.looping()) {
                if (channel.running) {
                    return false;
                }
                float stopElapsed = channel.stopAge - channel.startAge;
                float remaining = length - stopElapsed % length;
                return remaining > LOOP_TOLERANCE_TICKS || elapsed >= stopElapsed + remaining;
            }
            return elapsed >= length;
        }
    }

    private static final class Channel {
        private final Animation animation;
        private boolean running;
        private boolean pending;
        private boolean playing;
        private boolean hold;
        private float startAge;
        private float stopAge;
        private float weight;

        private Channel(Animation animation) {
            this.animation = animation;
        }
    }
}
