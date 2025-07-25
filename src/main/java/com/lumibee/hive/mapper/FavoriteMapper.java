package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.Favorites;
import org.apache.ibatis.annotations.Delete;

public interface FavoriteMapper extends BaseMapper<Favorites> {
    @Delete("DELETE FROM favorites WHERE id = #{id}")
    void removeById(Long id);
}
