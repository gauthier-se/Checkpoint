package com.seyzeriat.desktop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing the author of a news article returned by the admin API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsAuthorResult {
    private String id;
    private String pseudo;
    private String picture;

    public NewsAuthorResult() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
}
