package org.espen.collect.android.widgets.interfaces;

/**
 * @author James Knight
 */
public interface MultiChoiceWidget extends Widget {
    int getChoiceCount();

    void setChoiceSelected(int choiceIndex, boolean isSelected);
}
