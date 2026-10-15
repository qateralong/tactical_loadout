package tactical_loadout.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import tactical_loadout.grenade.SmokePuffOptions;

public class SmokePuffParticle extends TextureSheetParticle {
    private static final float MAX_ALPHA = 0.95F;
    private static final int FADE_IN = 12;
    private static final int FADE_OUT = 50;

    protected SmokePuffParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                                int lifetime, SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.friction = 0.9F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.quadSize = 1.3F + random.nextFloat() * 0.4F;
        this.lifetime = lifetime;
        float shade = 0.82F + random.nextFloat() * 0.1F;
        setColor(shade, shade, shade);
        this.alpha = 0.0F;
        pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (age < FADE_IN) {
            alpha = MAX_ALPHA * age / FADE_IN;
        } else if (age > lifetime - FADE_OUT) {
            alpha = MAX_ALPHA * Math.max(0, lifetime - age) / FADE_OUT;
        } else {
            alpha = MAX_ALPHA;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SmokePuffOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SmokePuffOptions options, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new SmokePuffParticle(level, x, y, z, xd, yd, zd, options.lifetime(), sprites);
        }
    }
}
