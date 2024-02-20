package org.waste.of.time.storage.cache

import it.unimi.dsi.fastutil.longs.LongArraySet
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.inventory.EnderChestInventory
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import org.waste.of.time.WorldTools.LOG
import org.waste.of.time.WorldTools.mc
import org.waste.of.time.storage.serializable.PlayerStoreable
import org.waste.of.time.storage.serializable.RegionBasedChunk
import org.waste.of.time.storage.serializable.RegionBasedEntities
import java.util.concurrent.ConcurrentHashMap

/**
 * [HotCache] that caches all currently loaded objects in the world that are needed for the world download.
 * It will be maintained until the user stops the capture process.
 * Then the data will be released into the storage data flow to be serialized in the storage thread.
 * This is needed because objects won't be saved to disk until they are unloaded from the world.
 */
object HotCache {
    val chunks = ConcurrentHashMap<ChunkPos, RegionBasedChunk>()
    internal val savedChunks = LongArraySet()
    val entities = ConcurrentHashMap<ChunkPos, MutableSet<EntityCacheable>>()
    val players: ConcurrentHashMap.KeySetView<PlayerStoreable, Boolean> = ConcurrentHashMap.newKeySet()
    val blockEntities: ConcurrentHashMap.KeySetView<LockableContainerBlockEntity, Boolean> =
        ConcurrentHashMap.newKeySet()
    var lastInteractedBlockEntity: BlockEntity? = null
    // TODO: load existing cached containers from already written chunks
    //  e.g. the player reloads a chunk that contains block entities that we already saved in the storage session
    //  The loading should be done async as it requires IO, so we should add some delay onto the debug rendering here
    //  so it doesn't confuse players as the box would pop in and then later disappear
    val cachedMissingContainers by LazyUpdatingDelegate(100) {
        chunks.values
            .flatMap { it.chunk.blockEntities.values }
            .filterIsInstance<LockableContainerBlockEntity>()
            .filter { it !in blockEntities }
    }
    // map id's of maps that we've seen during the capture
    val maps = HashSet<String>()

    fun getEntitySerializableForChunk(chunkPos: ChunkPos, world: World) =
        entities[chunkPos]?.let { entities ->
            RegionBasedEntities(chunkPos, entities, world)
        }

    fun convertEntities(world: World) =
        entities.map { entry ->
            RegionBasedEntities(entry.key, entry.value, world)
        }

    // used as public API for external mods like XaeroPlus, change carefully
    fun isChunkSaved(chunkX: Int, chunkZ: Int) = savedChunks.contains(ChunkPos.toLong(chunkX, chunkZ))

    fun clear() {
        chunks.clear()
        savedChunks.clear()
        entities.clear()
        players.clear()
        blockEntities.clear()
        lastInteractedBlockEntity = null
        // failing to reset this could cause users to accidentally save their echest contents on subsequent captures
        // todo: make a setting to configure this behavior
        if (!mc.isInSingleplayer)
            mc.player?.enderChestInventory = EnderChestInventory()
        maps.clear()
        LOG.info("Cleared hot cache")
    }
}
