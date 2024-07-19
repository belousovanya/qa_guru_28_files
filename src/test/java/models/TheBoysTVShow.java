package models;

public class TheBoysTVShow {
    @JsonProperty("title")
    private String title;

    @JsonProperty("seasons")
    private List<Season> seasons;


}
