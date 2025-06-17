package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.dto.CommentDTO;
import com.lumibee.hive.model.Comments;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comments> {

    @Select("SELECT c.*, u.name AS userName, u.avatar_url AS avatarUrl " +
            "FROM comments c " +
            "JOIN user u ON c.user_id = u.id " +
            "WHERE c.article_id = #{articleId} AND c.parent_comment_id IS NULL AND c.deleted = 0 " +
            "ORDER BY c.gmt_create DESC ")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "articleId", column = "article_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "parentCommentId", column = "parent_comment_id"),
            @Result(property = "rootCommentId", column = "root_comment_id"),
            @Result(property = "likesCount", column = "likes_count"),
            @Result(property = "gmtCreate", column = "gmt_create"),
            @Result(property = "userName", column = "userName"),
            @Result(property = "avatarUrl", column = "avatarUrl")
    })
    List<CommentDTO> getTopCommentsByArticleId(@Param("articleId") Integer articleId);

    @Select("SELECT c.*, u.name AS userName, u.avatar_url AS avatarUrl " +
            "FROM comments c " +
            "JOIN user u ON c.user_id = u.id " +
            "WHERE c.root_comment_id = #{rootCommentId} AND c.parent_comment_id IS NULL AND c.deleted = 0 " +
            "ORDER BY c.gmt_create DESC ")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "articleId", column = "article_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "parentCommentId", column = "parent_comment_id"),
            @Result(property = "rootCommentId", column = "root_comment_id"),
            @Result(property = "likesCount", column = "likes_count"),
            @Result(property = "gmtCreate", column = "gmt_create"),
            @Result(property = "userName", column = "userName"),
            @Result(property = "avatarUrl", column = "avatarUrl")
    })
    List<CommentDTO> getRepliesByRootCommentId(@Param("rootCommentId") Long rootCommentId);
    @Select({
            "<script>",
            "SELECT c.*, u.name as user_name, u.avatar_url ",
            "FROM comments c ",
            "JOIN user u ON c.user_id = u.id ",
            "WHERE c.root_comment_id IN ",
            "<foreach item='item' index='index' collection='rootIds' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "AND c.deleted = 0 ",
            "ORDER BY c.gmt_create ASC",
            "</script>"
    })
    List<CommentDTO> getAllRepliesByRootIds(@Param("rootIds") List<Long> rootIds);
}
