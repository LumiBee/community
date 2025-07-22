package com.lumibee.hive.controller;

import com.lumibee.hive.dto.FavoriteRequestDTO;
import com.lumibee.hive.dto.FavoriteDetailsDTO;
import com.lumibee.hive.dto.FavoriteResponse;
import com.lumibee.hive.model.Favorites;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.FavoriteService;
import com.lumibee.hive.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired private FavoriteService favoriteService;
    @Autowired private UserService userService;


    @GetMapping("/my-folders")
    public ResponseEntity<List<FavoriteDetailsDTO>> getMyFavorites(@AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        List<FavoriteDetailsDTO> favorites = favoriteService.getFavoritesByUserId(currentUser.getId());
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/add-to-folder")
    public ResponseEntity<FavoriteResponse> addArticleToFolder(@RequestBody FavoriteRequestDTO request,
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
    public ResponseEntity<FavoriteResponse> createAndAdd(@RequestBody FavoriteRequestDTO request,
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
    public ResponseEntity<?> createFolder(@RequestBody FavoriteRequestDTO request,
                                                         @AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        if (request.getFavoriteName() == null || request.getFavoriteName().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        favoriteService.createFavoriteAndAddArticle(
                currentUser.getId(), null, request.getFavoriteName()
        );

        List<FavoriteDetailsDTO> response = favoriteService.getFavoritesByUserId(currentUser.getId());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove-all/{articleId}")
    public ResponseEntity<Map<String, Object>> removeAllFromFolder(@PathVariable("articleId") Integer articleId,
                                                                    @AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> result = favoriteService.removeAllArticlesFromFavorite(currentUser.getId(), articleId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/details/{favoriteId}")
    public ResponseEntity<FavoriteDetailsDTO> getFavoriteDetails(@PathVariable("favoriteId") Long favoriteId) {
        FavoriteDetailsDTO favoriteDetails = favoriteService.selectPortfolioById(favoriteId);
        if (favoriteDetails == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(favoriteDetails);
    }
}