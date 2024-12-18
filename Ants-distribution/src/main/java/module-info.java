module ants {
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;
    requires java.desktop;

    opens org.evensen.ants to javafx.fxml;
	exports org.evensen.ants;
    exports org.evensen.ants.render;
    opens org.evensen.ants.render to javafx.fxml;
    exports org.evensen.ants.controller;
    opens org.evensen.ants.controller to javafx.fxml;
}