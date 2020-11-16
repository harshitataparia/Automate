package org.toolkit.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class FileCreationAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        InputDialogBox inputDialogBox = new InputDialogBox(e.getProject());

        inputDialogBox.showAndGet();
        if (inputDialogBox.isOK()) {
            inputDialogBox.close(23);
        }

    }
}
