package com.checkpoint.api.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.Formula;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Indexed
@Table(name = "lists")
public class GameList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @FullTextField
    @KeywordField(name = "titleSort", sortable = Sortable.YES)
    @Column(nullable = false)
    private String title;

    @FullTextField
    @Column(columnDefinition = "TEXT")
    private String description;

    @GenericField
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @GenericField(sortable = Sortable.YES)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // @Formula values are recomputed on entity load; @IndexingDependency(reindexOnUpdate = NO)
    // tells Hibernate Search not to auto-reindex on field change (it cannot detect formula
    // changes anyway). The index is refreshed by the startup mass-indexer and by explicit
    // addOrUpdate calls from GameListServiceImpl after add/remove/reorder operations.
    @GenericField(sortable = Sortable.YES)
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    @Formula("(SELECT COUNT(*) FROM game_list_entries gle WHERE gle.list_id = id)")
    private Integer videoGamesCount;

    // Refreshed from LikeServiceImpl after every toggle on a list. Same indexing strategy
    // as videoGamesCount: mass-index on startup, explicit addOrUpdate on change.
    @GenericField(sortable = Sortable.YES)
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.NO)
    @Formula("(SELECT COUNT(*) FROM likes l WHERE l.list_id = id)")
    private Integer likesCount;

    // Relationship: List is created by one user.
    // includeEmbeddedObjectId exposes the user's @Id as "user.id" (used for visibility=mine),
    // and includePaths adds "user.pseudo" for the author filter.
    @IndexedEmbedded(includePaths = {"pseudo"}, includeEmbeddedObjectId = true)
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relationship: List contains ordered entries (replaces ManyToMany)
    @OneToMany(mappedBy = "gameList", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<GameListEntry> entries = new ArrayList<>();

    // Relationship: List can have multiple comments
    @OneToMany(mappedBy = "gameList", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    // Relationship: List can have multiple likes
    @OneToMany(mappedBy = "gameList", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public GameList() {}

    public GameList(String title, User user) {
        this.title = title;
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<GameListEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<GameListEntry> entries) {
        this.entries = entries;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Set<Like> getLikes() {
        return likes;
    }

    public void setLikes(Set<Like> likes) {
        this.likes = likes;
    }

    public Integer getVideoGamesCount() {
        return videoGamesCount != null ? videoGamesCount : 0;
    }

    public Integer getLikesCount() {
        return likesCount != null ? likesCount : 0;
    }
}
