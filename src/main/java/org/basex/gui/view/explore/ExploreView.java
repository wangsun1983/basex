package org.basex.gui.view.explore;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import org.basex.data.Nodes;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;

/**
 * This view allows the input of database queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ExploreView extends View {
  /** Header string. */
  private final BaseXLabel header;
  /** Current search panel. */
  final ExploreArea search;
  /** Filter button. */
  final BaseXButton filter;

  /**
   * Default constructor.
   * @param man view manager
   */
  public ExploreView(final ViewNotifier man) {
    super(EXPLOREVIEW, HELPEXPLORE, man);

    setLayout(new BorderLayout(0, 4));
    setBorder(6, 8, 8, 8);
    setFocusable(false);

    header = new BaseXLabel(EXPLORETIT, true, false);

    final BaseXBack b = new BaseXBack(Fill.NONE);
    b.setLayout(new BorderLayout());
    b.add(header, BorderLayout.CENTER);

    filter = BaseXButton.command(GUICommands.FILTER, gui);
    filter.addKeyListener(this);

    b.add(filter, BorderLayout.EAST);
    add(b, BorderLayout.NORTH);

    search = new ExploreArea(this);
    add(search, BorderLayout.CENTER);

    refreshLayout();
  }

  @Override
  public void refreshInit() {
    search.init();
  }

  @Override
  public void refreshFocus() { }

  @Override
  public void refreshMark() {
    final Nodes marked = gui.context.marked;
    filter.setEnabled(!gui.prop.is(GUIProp.FILTERRT) &&
        marked != null && marked.size() != 0);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) { }

  @Override
  public void refreshUpdate() { }

  @Override
  public void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    refreshMark();
  }

  @Override
  public boolean visible() {
    return gui.prop.is(GUIProp.SHOWEXPLORE);
  }

  @Override
  protected boolean db() {
    return true;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    // overwrite default interactions
  }
}