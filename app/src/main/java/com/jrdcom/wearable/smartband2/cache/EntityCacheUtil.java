/**
 * Copyright (C) 2015 Fernando Cejas Open Source Project
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jrdcom.wearable.smartband2.cache;

import android.content.Context;

import com.jrdcom.wearable.smartband2.WearableApplication;
import com.jrdcom.wearable.smartband2.entity.BaseEntity;
import com.jrdcom.wearable.smartband2.executor.JobExecutor;
import com.jrdcom.wearable.smartband2.executor.ThreadExecutor;

import java.io.File;


public class EntityCacheUtil
{

    private static final String SETTINGS_FILE_NAME = "com.alcatel.movebond";
    private static final String SETTINGS_KEY_LAST_CACHE_UPDATE = "last_cache_update";
    private static Object mLock = new Object();


    private static final long EXPIRATION_TIME = 30 * 24 * 3600 * 1000;

    private Context context;
    private static File cacheDir;
    private static JsonSerializer serializer;
    private static FileManager fileManager;
    private static ThreadExecutor threadExecutor;

    private static EntityCacheUtil entityCacheUtil;


    /**
     * Constructor of the class {@link EntityCacheUtil}.
     * <p/>
     * <p/>
     * UserCacheSerializer {@link JsonSerializer} for object serialization.
     * {@link FileManager} for saving serialized objects to the file system.
     */
    public static EntityCacheUtil getInstance()
    {
        if (entityCacheUtil == null)
        {
            synchronized (EntityCacheUtil.class)
            {
                if (entityCacheUtil == null)
                {
                    entityCacheUtil = new EntityCacheUtil();
                }
            }
        }
        return entityCacheUtil;

    }

    private EntityCacheUtil()
    {
        this(WearableApplication.getInstance(), new JsonSerializer(), new FileManager(), JobExecutor.getInstance());
    }

    /**
     * Constructor of the class {@link EntityCacheUtil}.
     *
     * @param context                A
     * @param accountCacheSerializer {@link JsonSerializer} for object serialization.
     * @param fileManager            {@link FileManager} for saving serialized objects to the file system.
     */
    private EntityCacheUtil(Context context, JsonSerializer accountCacheSerializer,
                            FileManager fileManager, ThreadExecutor executor)
    {
        if (context == null || accountCacheSerializer == null || fileManager == null || executor == null)
        {
            throw new IllegalArgumentException("Invalid null parameter");
        }
        this.context = context.getApplicationContext();
        this.cacheDir = this.context.getCacheDir();
        this.serializer = accountCacheSerializer;
        this.fileManager = fileManager;
        this.threadExecutor = executor;
    }

    public void setSerializer(JsonSerializer serializer)
    {
        this.serializer = serializer;
    }

    public void setFileManager(FileManager fileManager)
    {
        this.fileManager = fileManager;
    }

    public void setThreadExecutor(ThreadExecutor threadExecutor)
    {
        this.threadExecutor = threadExecutor;
    }

    public static <T extends BaseEntity> T get(T t)
    {
        synchronized (mLock){
            File accountEntityFile = EntityCacheUtil.buildFile(t);
            String fileContent = EntityCacheUtil.fileManager.readFileContent(accountEntityFile);
            T tCreate = (T) EntityCacheUtil.serializer.deserialize(fileContent, t.getClass());
            if (tCreate == null)
            {
                try
                {
                    tCreate = (T) Class.forName(t.getClass().getName()).newInstance();
                } catch (InstantiationException e)
                {
                    e.printStackTrace();
                } catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                } catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
            if (tCreate != null)
            {
                return tCreate;
            } else
            {
                return null;
            }
        }
    }

    public static <T extends BaseEntity> void putLocal(T t)
    {
        synchronized (mLock){
            if (t != null)
            {
                File entitiyFile = buildFile(t);
//			if (!isCached(t)) {
                String jsonString = serializer.serialize(t);
                executeAsynchronously(new CacheWriter(fileManager, entitiyFile, jsonString));
                setLastCacheUpdateTimeMillis();
            }
        }
    }


    public static <T extends BaseEntity> boolean isCached(T t)
    {
        File entitiyFile = EntityCacheUtil.buildFile(t);
        boolean isCache = EntityCacheUtil.fileManager.exists(entitiyFile);
        return isCache;
    }

    public static boolean isExpired()
    {
        long currentTime = System.currentTimeMillis();
        long lastUpdateTime = EntityCacheUtil.getLastCacheUpdateTimeMillis();

        boolean expired = ((currentTime - lastUpdateTime) > EXPIRATION_TIME);

        if (expired)
        {
            EntityCacheUtil.evictAll();
        }

        return expired;
    }

    public static synchronized void evictAll()
    {
        EntityCacheUtil.executeAsynchronously(new CacheEvictor(EntityCacheUtil.fileManager, EntityCacheUtil.cacheDir));
    }

    /**
     * Build a file, used to be inserted in the disk cache.
     *
     * @param t The id user to build the file.
     * @return A valid file.
     */
    private static <T extends BaseEntity> File buildFile(T t)
    {
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(EntityCacheUtil.cacheDir.getPath());
        fileNameBuilder.append(File.separator);
        fileNameBuilder.append(t.getEntityDefaultFileName() + "_");
        fileNameBuilder.append(t.getEntityId());

        return new File(fileNameBuilder.toString());
    }

    /**
     * Set in millis, the last time the cache was accessed.
     */
    private static void setLastCacheUpdateTimeMillis()
    {
        long currentMillis = System.currentTimeMillis();
        EntityCacheUtil.fileManager.writeToPreferences(WearableApplication.getInstance(), SETTINGS_FILE_NAME,
                SETTINGS_KEY_LAST_CACHE_UPDATE, currentMillis);
    }

    /**
     * Get in millis, the last time the cache was accessed.
     */
    private static long getLastCacheUpdateTimeMillis()
    {
        return EntityCacheUtil.fileManager.getFromPreferences(WearableApplication.getInstance(), SETTINGS_FILE_NAME,
                SETTINGS_KEY_LAST_CACHE_UPDATE);
    }

    /**
     * Executes a {@link Runnable} in another Thread.
     *
     * @param runnable {@link Runnable} to execute
     */
    private static void executeAsynchronously(Runnable runnable)
    {
        EntityCacheUtil.threadExecutor.execute(runnable);
    }

    /**
     * {@link Runnable} class for writing to disk.
     */
    private static class CacheWriter implements Runnable
    {
        private final FileManager fileManager;
        private final File fileToWrite;
        private final String fileContent;

        CacheWriter(FileManager fileManager, File fileToWrite, String fileContent)
        {
            this.fileManager = fileManager;
            this.fileToWrite = fileToWrite;
            this.fileContent = fileContent;
        }

        @Override
        public void run()
        {
            this.fileManager.writeToFile(fileToWrite, fileContent);
        }
    }

    /**
     * {@link Runnable} class for evicting all the cached files
     */
    private static class CacheEvictor implements Runnable
    {
        private final FileManager fileManager;
        private final File cacheDir;

        CacheEvictor(FileManager fileManager, File cacheDir)
        {
            this.fileManager = fileManager;
            this.cacheDir = cacheDir;
        }

        @Override
        public void run()
        {
            this.fileManager.clearDirectory(this.cacheDir);
        }
    }
}
