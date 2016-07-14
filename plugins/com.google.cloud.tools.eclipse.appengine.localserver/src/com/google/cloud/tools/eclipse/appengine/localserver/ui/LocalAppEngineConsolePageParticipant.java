package com.google.cloud.tools.eclipse.appengine.localserver.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.eclipse.wst.server.ui.internal.Messages;

/**
 * Adds a stop button for the App Engine runtime to the {@link LocalAppEngineConsole}
 */
@SuppressWarnings("restriction") // For ImageResource, Messages
public class LocalAppEngineConsolePageParticipant implements IConsolePageParticipant {
  private LocalAppEngineConsole console;
  private Action terminateAction;
  
  @Override
  public <T> T getAdapter(Class<T> required) {
    return null;
  }

  @Override
  public void init(IPageBookViewPage page, IConsole console) {
    this.console = (LocalAppEngineConsole)console;
    
    // contribute to toolbar
    IActionBars actionBars = page.getSite().getActionBars();
    configureToolBar(actionBars.getToolBarManager());
  }

  @Override
  public void dispose() {
    terminateAction = null;
  }

  @Override
  public void activated() {
    update();
  }

  @Override
  public void deactivated() {
    update();
  }
  
  private void configureToolBar(IToolBarManager mgr) {
    terminateAction = new Action(Messages.actionStop) {
        public void run() {
            //code to execute when button is pressed
          console.getServerBehaviourDeelgate().stop(true);
          update();
        }
    };
    terminateAction.setToolTipText(Messages.actionStopToolTip);
    terminateAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_STOP));
    terminateAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_STOP));
    terminateAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_STOP));
    
    mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, terminateAction);
  }
  
  private void update() {
    if (terminateAction != null) {
      IStatus status = console.getServerBehaviourDeelgate().canStop();
      terminateAction.setEnabled(status.isOK());
    }
  }
 
}
