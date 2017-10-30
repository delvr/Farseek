package com.pg85.otg.generator;

import com.pg85.otg.forge.ForgeWorld;

public abstract class ChunkProviderOTG {
    private ForgeWorld localWorld;
    protected abstract void generateTerrain(ChunkBuffer chunkBuffer);
}
