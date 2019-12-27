import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;


public class HelloAction extends AnAction {
    public HelloAction() {
        super("Hello");
    }

    public void actionPerformed(AnActionEvent event) {
        // Clicked the action in the menu.
        Project project = event.getProject();
        if (project != null) {
            Messages.showMessageDialog(project, "HelloWorld", "Greeting", Messages.getInformationIcon());

        }
    }
}

