package com.checkpoint.api.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.checkpoint.api.enums.PlayStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Association entity between User and VideoGame.
 * Represents a user playing a specific game on a specific platform.
 */
@Entity
@Table(name = "user_game_plays")
public class UserGamePlay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayStatus status = PlayStatus.ARE_PLAYING;

    @Column(name = "is_replay", nullable = false)
    private Boolean isReplay = false;

    @Column(name = "time_played")
    private Integer timePlayed; // Time played in minutes

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private String ownership; // e.g., "owned", "borrowed", "subscription", "pirated"

    @Column(name = "score")
    @Min(1)
    @Max(5)
    private Integer score;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship: UserGamePlay belongs to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relationship: UserGamePlay is for one video game
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_game_id", nullable = false)
    private VideoGame videoGame;

    // Relationship: UserGamePlay is on one platform
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    // Relationship: UserGamePlay can have one optional review
    @OneToOne(mappedBy = "userGamePlay", cascade = CascadeType.ALL, orphanRemoval = true)
    private Review review;

    // Relationship: UserGamePlay can have multiple tags
    @ManyToMany
    @JoinTable(
        name = "user_game_play_tags",
        joinColumns = @JoinColumn(name = "user_game_play_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UserGamePlay() {}

    public UserGamePlay(User user, VideoGame videoGame, Platform platform) {
        this.user = user;
        this.videoGame = videoGame;
        this.platform = platform;
    }

    public UserGamePlay(User user, VideoGame videoGame, Platform platform, PlayStatus status) {
        this.user = user;
        this.videoGame = videoGame;
        this.platform = platform;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlayStatus getStatus() {
        return status;
    }

    public void setStatus(PlayStatus status) {
        this.status = status;
    }

    public Boolean getIsReplay() {
        return isReplay;
    }

    public void setIsReplay(Boolean isReplay) {
        this.isReplay = isReplay;
    }

    public Integer getTimePlayed() {
        return timePlayed;
    }

    public void setTimePlayed(Integer timePlayed) {
        this.timePlayed = timePlayed;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getOwnership() {
        return ownership;
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
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

    public VideoGame getVideoGame() {
        return videoGame;
    }

    public void setVideoGame(VideoGame videoGame) {
        this.videoGame = videoGame;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    /**
     * Adds a tag to this play log (bidirectional).
     *
     * @param tag the tag to add
     */
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getPlayLogs().add(this);
    }

    /**
     * Removes a tag from this play log (bidirectional).
     *
     * @param tag the tag to remove
     */
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getPlayLogs().remove(this);
    }
}
