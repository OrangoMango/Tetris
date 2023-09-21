// File managed by WebFX (DO NOT EDIT MANUALLY)

module Tetris.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;
    requires webfx.platform.console;
    requires webfx.platform.os;
    requires webfx.platform.resource;
    requires webfx.platform.scheduler;
    requires webfx.platform.storage;

    // Exported packages
    exports com.orangomango.tetris;

    // Resources packages
    opens audio;
    opens fonts;
    opens images;

    // Provided services
    provides javafx.application.Application with com.orangomango.tetris.MainApplication;

}