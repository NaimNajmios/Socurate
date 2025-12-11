package com.najmi.oreamnos.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for MainActivity.
 * Holds UI state that survives configuration changes (like screen rotation).
 * Decouples MainActivity from business logic and state management.
 */
public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";

    // Current generation state
    private final MutableLiveData<GenerationState> state = new MutableLiveData<>(GenerationState.idle());

    // Original input text for regeneration
    private String originalInputText = "";

    // Store the original generated content (before user edits)
    private String originalGeneratedPost = "";

    // Edit mode state
    private boolean isEditMode = false;

    // Current input text (for restoration)
    private String currentInputText = "";

    // User-edited content (if different from original)
    private String userEditedContent = "";

    /**
     * Gets the observable generation state.
     */
    public LiveData<GenerationState> getState() {
        return state;
    }

    /**
     * Gets the current state value (non-observable).
     */
    public GenerationState getCurrentState() {
        return state.getValue();
    }

    /**
     * Sets the state to loading.
     *
     * @param isRefinement Whether this is a refinement operation
     */
    public void setLoading(boolean isRefinement) {
        state.setValue(GenerationState.loading(isRefinement));
    }

    /**
     * Sets the state to success with generated content.
     *
     * @param title        Generated title
     * @param body         Generated body
     * @param source       Source citation
     * @param isRefinement Whether this was a refinement
     */
    public void setSuccess(String title, String body, String source, boolean isRefinement) {
        state.setValue(GenerationState.success(title, body, source, isRefinement));
    }

    /**
     * Sets the state to error.
     *
     * @param errorMessage Error description
     * @param isRefinement Whether this was a refinement
     */
    public void setError(String errorMessage, boolean isRefinement) {
        state.setValue(GenerationState.error(errorMessage, isRefinement));
    }

    /**
     * Resets the state to idle.
     */
    public void resetState() {
        state.setValue(GenerationState.idle());
        originalInputText = "";
        originalGeneratedPost = "";
        currentInputText = "";
        userEditedContent = "";
        isEditMode = false;
    }

    /**
     * Gets the original input text.
     */
    public String getOriginalInputText() {
        return originalInputText;
    }

    /**
     * Sets the original input text.
     */
    public void setOriginalInputText(String text) {
        this.originalInputText = text != null ? text : "";
    }

    /**
     * Gets the original generated post (before user edits).
     */
    public String getOriginalGeneratedPost() {
        return originalGeneratedPost;
    }

    /**
     * Sets the original generated post.
     */
    public void setOriginalGeneratedPost(String post) {
        this.originalGeneratedPost = post != null ? post : "";
    }

    /**
     * Checks if there is content available.
     */
    public boolean hasContent() {
        GenerationState currentState = state.getValue();
        return currentState != null && currentState.hasContent();
    }

    /**
     * Gets the edit mode state.
     */
    public boolean isEditMode() {
        return isEditMode;
    }

    /**
     * Sets the edit mode state.
     */
    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
    }

    /**
     * Gets the current input text.
     */
    public String getCurrentInputText() {
        return currentInputText;
    }

    /**
     * Sets the current input text.
     */
    public void setCurrentInputText(String text) {
        this.currentInputText = text != null ? text : "";
    }

    /**
     * Gets user-edited content (if different from original).
     */
    public String getUserEditedContent() {
        return userEditedContent;
    }

    /**
     * Sets user-edited content.
     */
    public void setUserEditedContent(String content) {
        this.userEditedContent = content != null ? content : "";
    }
}
