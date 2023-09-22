// File managed by WebFX (DO NOT EDIT MANUALLY)

module Tetris.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires webfx.extras.webtext;
    requires webfx.platform.fetch;
    requires webfx.platform.json;
    requires webfx.platform.os;
    requires webfx.platform.resource;
    requires webfx.platform.scheduler;
    requires webfx.platform.storage;
    requires webfx.stack.ui.controls;

    // Exported packages
    exports com.orangomango.tetris;

    // Resources packages
    opens audio;
    opens fonts;
    opens images;

    // Provided services
    provides javafx.application.Application with com.orangomango.tetris.MainApplication;

}