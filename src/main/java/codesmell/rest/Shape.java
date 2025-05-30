package codesmell.rest;

public class Shape {

    private Long id;
    private String shape;
    private String color;
    private String description;

    private Shape() {
        // force use of builder
    }

    public static ShapeBuilder builder() {
        return new Shape.ShapeBuilder();
    }

    public static class ShapeBuilder {
        private Shape managedInstance = new Shape();

        public ShapeBuilder id(Long id) {
            managedInstance.id = id;
            return this;
        }

        public ShapeBuilder shape(String shape) {
            managedInstance.shape = shape;
            return this;
        }

        public ShapeBuilder color(String color) {
            managedInstance.color = color;
            return this;
        }

        public ShapeBuilder description(String description) {
            managedInstance.description = description;
            return this;
        }
        
        public Shape build() {
            return managedInstance;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
