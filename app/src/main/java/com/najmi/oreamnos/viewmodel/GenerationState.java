package com.najmi.oreamnos.viewmodel;

/**
 * Immutable state class representing the UI state for content generation.
 * Used by MainViewModel to communicate state changes to MainActivity.
 */
public class GenerationState {

    /**
     * Possible states of the generation process.
     */
    public enum Status {
        /** Initial state, no generation in progress */
        IDLE,
        /** Generation or refinement in progress */
        LOADING,
        /** Generation completed successfully */
        SUCCESS,
        /** Generation failed */
        ERROR
    }

    private final Status status;
    private final String generatedTitle;
    private final String generatedBody;
    private final String sourceCitation;
    private final String errorMessage;
    private final boolean isRefinement;

    private GenerationState(Builder builder) {
        this.status = builder.status;
        this.generatedTitle = builder.generatedTitle;
        this.generatedBody = builder.generatedBody;
        this.sourceCitation = builder.sourceCitation;
        this.errorMessage = builder.errorMessage;
        this.isRefinement = builder.isRefinement;
    }

    // Getters
    public Status getStatus() {
        return status;
    }

    public String getGeneratedTitle() {
        return generatedTitle;
    }

    public String getGeneratedBody() {
        return generatedBody;
    }

    public String getSourceCitation() {
        return sourceCitation;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isRefinement() {
        return isRefinement;
    }

    /**
     * Checks if there is generated content available.
     */
    public boolean hasContent() {
        return (generatedTitle != null && !generatedTitle.isEmpty()) ||
                (generatedBody != null && !generatedBody.isEmpty());
    }

    // Factory methods for common states
    public static GenerationState idle() {
        return new Builder().status(Status.IDLE).build();
    }

    public static GenerationState loading(boolean isRefinement) {
        return new Builder()
                .status(Status.LOADING)
                .isRefinement(isRefinement)
                .build();
    }

    public static GenerationState success(String title, String body, String source, boolean isRefinement) {
        return new Builder()
                .status(Status.SUCCESS)
                .generatedTitle(title)
                .generatedBody(body)
                .sourceCitation(source)
                .isRefinement(isRefinement)
                .build();
    }

    public static GenerationState error(String message, boolean isRefinement) {
        return new Builder()
                .status(Status.ERROR)
                .errorMessage(message)
                .isRefinement(isRefinement)
                .build();
    }

    /**
     * Builder pattern for creating GenerationState instances.
     */
    public static class Builder {
        private Status status = Status.IDLE;
        private String generatedTitle = "";
        private String generatedBody = "";
        private String sourceCitation = "";
        private String errorMessage = "";
        private boolean isRefinement = false;

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder generatedTitle(String title) {
            this.generatedTitle = title != null ? title : "";
            return this;
        }

        public Builder generatedBody(String body) {
            this.generatedBody = body != null ? body : "";
            return this;
        }

        public Builder sourceCitation(String source) {
            this.sourceCitation = source != null ? source : "";
            return this;
        }

        public Builder errorMessage(String error) {
            this.errorMessage = error != null ? error : "";
            return this;
        }

        public Builder isRefinement(boolean isRefinement) {
            this.isRefinement = isRefinement;
            return this;
        }

        public GenerationState build() {
            return new GenerationState(this);
        }
    }
}
