package org.odk.collect.lookup;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LookUpRepository {

    @Nullable
    LookUp get(Long id);

//    @Nullable
//    LookUp getLatestByLookUpIdAndVersion(String lookUpId, @Nullable String version);
//
//    @Nullable
//    LookUp getOneByPath(String path);
//
//    @Nullable
//    LookUp getOneByMd5Hash(@NotNull String hash);

//    List<LookUp> getAll();
//
//    List<LookUp> getAllByLookUpIdAndVersion(String lookUpId, @Nullable String version);
//
//    List<LookUp> getAllByLookUpId(String lookUpId);
//
//    List<LookUp> getAllNotDeletedByLookUpId(String lookUpId);
//
//    List<LookUp> getAllNotDeletedByLookUpIdAndVersion(String lookUpId, @Nullable String version);

//    LookUp save(@NotNull LookUp form);

    void delete(Long id);

    void save(LookUp lookupFromValues);

    List<String> getBySearchQuery(String projection, String selection, String[] selectionArgs);
//    void softDelete(Long id);

//    void deleteByMd5Hash(@NotNull String md5Hash);
//
//    void deleteAll();
//
//    void restore(Long id);
}
