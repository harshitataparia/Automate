package org.toolkit.action;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.uiDesigner.core.AbstractLayout;
import com.intellij.util.ui.GridBag;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputDialogBox extends DialogWrapper
{
    JPanel panel = new JPanel(new GridBagLayout());
    JTextField packageNameTextField = new JTextField();
    JTextField classNameTextField = new JTextField();
    JTextField methodNameTextField = new JTextField();


    Project project;

    public InputDialogBox(Project project) {
        super(project);
        this.project = project;
        super.init();
        setTitle("Project Details");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        GridBag gb = new GridBag().setDefaultInsets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP)
                .setDefaultWeightX(1.0).setDefaultFill(GridBagConstraints.HORIZONTAL);
        GridBagConstraints c = new GridBagConstraints();
        panel.setPreferredSize(new Dimension(500, 200));

        panel.add(new JLabel("Package Name :"), gb.nextLine().next().weightx(0.2));
        panel.add(packageNameTextField, gb.next().weightx(0.8));
        panel.add(new JLabel("Class Name :"), gb.nextLine().next().weightx(0.2));
        panel.add(classNameTextField, gb.next().weightx(0.8));
        panel.add(new JLabel("Method Name :"), gb.nextLine().next().weightx(0.2));
        panel.add(methodNameTextField, gb.next().weightx(0.8));

        return panel;
    }
    @Nullable
    protected ValidationInfo doValidate() {
        if (StringUtil.isEmpty(classNameTextField.getText())) {
            return new ValidationInfo("Class Name Required", classNameTextField);
        } else {
            String className = classNameTextField.getText();
            Matcher m = Pattern.compile("[^a-zA-Z]").matcher(className);
            if (className.length() < 6 || m.find()) {
                return new ValidationInfo("Invalid Class Name", classNameTextField);
            }
            if (Character.isLowerCase(className.charAt(0))) {
                return new ValidationInfo("First character of class should be in Uppercase", classNameTextField);
            }
        }

        if (StringUtil.isEmpty(packageNameTextField.getText())) {
            return new ValidationInfo("Package Name Required", packageNameTextField);
        }
        if (StringUtil.isEmpty(methodNameTextField.getText())) {
            return new ValidationInfo("Method Name Required", methodNameTextField);
        }
        return null;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        String packageName = packageNameTextField.getText();
        String className = classNameTextField.getText();
        String methodName = methodNameTextField.getText();

        FileTemplate classTemplate = FileTemplateManager.getDefaultInstance().getTemplate("SampleFileFormat");
        System.out.println("ClassTemplate - "+classTemplate);
        Properties propertiesMap = FileTemplateManager.getDefaultInstance().getDefaultProperties();
        propertiesMap.setProperty("packageName", packageName);
        propertiesMap.setProperty("ClassName", className);
        propertiesMap.setProperty("MethodName", methodName);

        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(project.getBasePath());
        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(virtualFile);
        PsiDirectory javaDir = directory.findSubdirectory("src").findSubdirectory("main").findSubdirectory("java");

        String[] packagePathNames = packageName.split("\\.");
        PsiDirectory finalDir = javaDir;
        for (String dirName : packagePathNames) {
            if (finalDir.findSubdirectory(dirName) == null) {

                PsiDirectory finalDir1 = finalDir;
                Runnable createDirAction = new Runnable() {
                    @Override
                    public void run() {
                        finalDir1.createSubdirectory(dirName);
                    }
                };
                ApplicationManager.getApplication().runWriteAction(createDirAction);
                finalDir = finalDir.findSubdirectory(dirName);
            } else {
                finalDir = finalDir.findSubdirectory(dirName);
            }
        }
        try {
            FileTemplateUtil.createFromTemplate(classTemplate, className, propertiesMap, finalDir);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
