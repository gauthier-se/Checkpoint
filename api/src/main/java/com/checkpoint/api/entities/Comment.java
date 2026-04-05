package com.checkpoint.api.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

/**
 * Comment entity that can be associated with either a GameList or a Review.
 * A comment belongs to exactly one of these (polymorphic relationship).
 */
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship: Comment is posted by one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relationship: Comment can be on a list (nullable - polymorphic)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id")
    private GameList gameList;

    // Relationship: Comment can be on a review (nullable - polymorphic)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    // Relationship: Comment can be a reply to another comment (nullable for top-level)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    // Relationship: A parent comment has many replies, ordered by creation time
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> replies = new ArrayList<>();

    // Relationship: Comment can have multiple likes
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    // Relationship: Comment can have multiple reports
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Report> reports = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Comment() {}

    public Comment(String content, User user) {
        this.content = content;
        this.user = user;
    }

    // Factory methods for clarity
    public static Comment onList(String content, User user, GameList gameList) {
        Comment comment = new Comment(content, user);
        comment.setGameList(gameList);
        return comment;
    }

    public static Comment onReview(String content, User user, Review review) {
        Comment comment = new Comment(content, user);
        comment.setReview(review);
        return comment;
    }

    /**
     * Creates a reply to an existing comment. Enforces 1-level nesting: if the
     * parent is itself a reply, the new reply targets the root parent instead.
     * The reply inherits the parent's review or list association.
     *
     * @param content       the reply text content
     * @param user          the reply author
     * @param parentComment the comment being replied to
     * @return the new reply comment
     */
    public static Comment asReply(String content, User user, Comment parentComment) {
        Comment comment = new Comment(content, user);
        Comment effectiveParent = parentComment.getParentComment() != null
                ? parentComment.getParentComment()
                : parentComment;
        comment.setParentComment(effectiveParent);
        comment.setReview(effectiveParent.getReview());
        comment.setGameList(effectiveParent.getGameList());
        return comment;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public GameList getGameList() {
        return gameList;
    }

    public void setGameList(GameList gameList) {
        this.gameList = gameList;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }

    public Set<Like> getLikes() {
        return likes;
    }

    public void setLikes(Set<Like> likes) {
        this.likes = likes;
    }

    public Set<Report> getReports() {
        return reports;
    }

    public void setReports(Set<Report> reports) {
        this.reports = reports;
    }
}
