package dialog;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DemoDialogWrapper extends DialogWrapper {

    public DemoDialogWrapper() {
        super(true); // use current window as parent
        init();
        setTitle("Test DialogWrapper");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Would you like to close the Demo Tools Panel?");
        label.setPreferredSize(new Dimension(150, 50));
        dialogPanel.add(label, BorderLayout.CENTER);

        return dialogPanel;
    }
}
