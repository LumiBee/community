package com.lumibee.hive.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.dto.FavoriteDetailsDTO;
import com.lumibee.hive.dto.FavoriteRequestDTO;
import com.lumibee.hive.dto.FavoriteResponse;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.FavoriteService;
import com.lumibee.hive.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/favorites")
@Tag(name = "收藏管理", description = "收藏相关的 API 接口")
public class FavoriteController {

    @Autowired private FavoriteService favoriteService;
    @Autowired private UserService userService;

    @GetMapping("/my-folders")
    @Operation(summary = "获取我的收藏夹", description = "获取当前用户的所有收藏夹")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<List<FavoriteDetailsDTO>> getMyFavorites(@AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        List<FavoriteDetailsDTO> favorites = favoriteService.getFavoritesByUserId(currentUser.getId());
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/add-to-folder")
    @Operation(summary = "添加文章到收藏夹", description = "将文章添加到指定的收藏夹中")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "添加成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<FavoriteResponse> addArticleToFolder(
            @Parameter(description = "收藏请求参数") @RequestBody FavoriteRequestDTO request,
            @AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        FavoriteResponse response = favoriteService.addArticleToFavorite(
                currentUser.getId(), request.getArticleId(), request.getFavoriteId()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-and-add")
    @Operation(summary = "创建收藏夹并添加文章", description = "创建新的收藏夹并将文章添加到其中")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建并添加成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<FavoriteResponse> createAndAdd(
            @Parameter(description = "收藏请求参数") @RequestBody FavoriteRequestDTO request,
            @AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        FavoriteResponse response = favoriteService.createFavoriteAndAddArticle(
                currentUser.getId(), request.getArticleId(), request.getFavoriteName()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-folder")
    @Operation(summary = "创建收藏夹", description = "创建新的收藏夹")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<List<FavoriteDetailsDTO>> createFolder(
            @Parameter(description = "收藏夹创建请求") @RequestBody FavoriteRequestDTO request,
            @AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        if (request.getFavoriteName() == null || request.getFavoriteName().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        favoriteService.createFavorite(
                request.getFavoriteName(), currentUser.getId()
        );

        List<FavoriteDetailsDTO> response = favoriteService.getFavoritesByUserId(currentUser.getId());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove-all/{articleId}")
    @Operation(summary = "从所有收藏夹移除文章", description = "从用户的所有收藏夹中移除指定文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "移除成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Map<String, Object>> removeAllFromFolder(
            @Parameter(description = "文章ID") @PathVariable("articleId") Integer articleId,
            @AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> result = favoriteService.removeAllArticlesFromFavorite(currentUser.getId(), articleId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/remove-from-folder/{articledId}/{favoriteId}")
    @Operation(summary = "从指定收藏夹移除文章", description = "从指定的收藏夹中移除文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "移除成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Map<String, Object>> removeFromFolder(
            @Parameter(description = "文章ID") @PathVariable("articledId") Integer articledId,
            @Parameter(description = "收藏夹ID") @PathVariable("favoriteId") Long favoriteId,
            @AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> result = favoriteService.removeArticleFromFavorite(currentUser.getId(), articledId, favoriteId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/remove-folder/{favoriteId}")
    @Operation(summary = "删除收藏夹", description = "删除指定的收藏夹")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Map<String, Object>> removeFolder(@PathVariable("favoriteId") Integer favoriteId,
                                                             @AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> result = favoriteService.removeFolderFromFavorite(currentUser.getId(), favoriteId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update-folder/{favoriteId}")
    @Operation(summary = "更新收藏夹名称", description = "更新指定收藏夹的名称")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Map<String, Object>> updateFolder(@PathVariable("favoriteId") Integer favoriteId,
                                                           @RequestBody FavoriteRequestDTO request,
                                                           @AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        if (request.getFavoriteName() == null || request.getFavoriteName().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "收藏夹名称不能为空"));
        }
        
        Map<String, Object> result = favoriteService.updateFavoriteFolder(currentUser.getId(), favoriteId, request.getFavoriteName());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/details/{favoriteId}")
    @Operation(summary = "获取收藏夹详情", description = "根据收藏夹ID获取收藏夹的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "收藏夹不存在")
    })
    public ResponseEntity<FavoriteDetailsDTO> getFavoriteDetails(@PathVariable("favoriteId") Long favoriteId) {
        FavoriteDetailsDTO favoriteDetails = favoriteService.selectFavoritesById(favoriteId);
        if (favoriteDetails == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(favoriteDetails);
    }
}