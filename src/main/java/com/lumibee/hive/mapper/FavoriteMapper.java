package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.model.Favorites;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FavoriteMapper extends BaseMapper<Favorites> {

}
