package digital.naomie.f1bot.utilities;

public class Series {
    private final String name;
    private final String url;
    private final String color;
    private final String logo;

    public Series(String url, String name, String color, String logo) {
        this.name = name;
        this.url = url;
        this.color = color;
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getColor() {
        return color;
    }

    public String getLogo() {
        return String.format("/images/%s", logo);
    }

}
