package net.more_rpg_classes.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.spell_engine.Platform;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.more_rpg_classes.worldgen.ModStructureTypes;

import java.util.Optional;

public class ConditionalJigsawStructure extends Structure {
    public static final int MAX_SIZE = 128;

    // 1.20.1: StructureType#codec() returns a plain Codec, and the jigsaw structure carries no
    // dimension padding / liquid settings (both are 1.21 additions) - those fields are simply absent here.
    public static final Codec<ConditionalJigsawStructure> CODEC = RecordCodecBuilder.<ConditionalJigsawStructure>mapCodec(instance ->
            instance.group(
                    configCodecBuilder(instance),
                    Codec.STRING.fieldOf("mod_id").forGetter(s -> s.modId),
                    StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
                    Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(s -> s.startJigsawName),
                    Codec.intRange(0, MAX_SIZE).fieldOf("size").forGetter(s -> s.size),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
                    Codec.BOOL.optionalFieldOf("use_expansion_hack", false).forGetter(s -> s.useExpansionHack),
                    Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
                    Codec.intRange(1, MAX_SIZE).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter)
            ).apply(instance, ConditionalJigsawStructure::new)
    ).codec();

    private final String modId;
    private final RegistryEntry<StructurePool> startPool;
    private final Optional<Identifier> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Type> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public ConditionalJigsawStructure(
            Config config,
            String modId,
            RegistryEntry<StructurePool> startPool,
            Optional<Identifier> startJigsawName,
            int size,
            HeightProvider startHeight,
            boolean useExpansionHack,
            Optional<Heightmap.Type> projectStartToHeightmap,
            int maxDistanceFromCenter
    ) {
        super(config);
        this.modId = modId;
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        if (!Platform.util().isModLoaded(modId)) {
            return Optional.empty();
        }

        ChunkPos chunkPos = context.chunkPos();
        HeightContext heightContext = new HeightContext(context.chunkGenerator(), context.world());
        int y = this.startHeight.get(context.random(), heightContext);
        BlockPos blockPos = new BlockPos(chunkPos.getStartX(), y, chunkPos.getStartZ());

        return StructurePoolBasedGenerator.generate(
                context,
                this.startPool,
                this.startJigsawName,
                this.size,
                blockPos,
                this.useExpansionHack,
                this.projectStartToHeightmap,
                this.maxDistanceFromCenter
        );
    }

    @Override
    public StructureType<?> getType() {
        return ModStructureTypes.CONDITIONAL_JIGSAW;
    }

    public String getModId() {
        return modId;
    }
}
