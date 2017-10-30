package com.pg85.otg.forge.generator;

import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.generator.ChunkProviderOTG;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.List;

public class OTGChunkGenerator implements IChunkGenerator {
    private ChunkProviderOTG generator;
    public OTGChunkGenerator(ForgeWorld world) {}

    public Chunk generateChunk(int i, int i1) {
        return null;
    }

    public void populate(int i, int i1) { }

    public boolean generateStructures(Chunk chunk, int i, int i1) {
        return false;
    }

    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType enumCreatureType, BlockPos blockPos) {
        return null;
    }

    public BlockPos getNearestStructurePos(World world, String s, BlockPos blockPos, boolean b) {
        return null;
    }

    public void recreateStructures(Chunk chunk, int i, int i1) { }

    public boolean isInsideStructure(World world, String s, BlockPos blockPos) {
        return false;
    }
}
