package org.basex.gui.view.query;

import static org.basex.gui.GUIConstants.*;
import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import org.basex.core.Commands;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.util.Action;
import org.basex.util.Token;

/**
 * This class provides a text area for entering queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class QueryArea extends QueryPanel {
  /** Error pattern. */
  static final Pattern ERRPATTERN = Pattern.compile(
      ".* line ([0-9]+), column ([0-9]+).*", Pattern.DOTALL);
  /** Info label. */
  BaseXLabel info;
  /** Main panel. */
  QueryView main;
  /** Text Area. */
  BaseXText area;
  /** Filter/Execute Button. */
  BaseXButton filter;
  /** Scroll Pane. */
  BaseXBack south;

  /**
   * Default constructor.
   * @param view main panel
   */
  QueryArea(final QueryView view) {
    main = view;
    area = new BaseXText(HELPQUERYMODE);
    area.setSyntax(new QuerySyntax());
    area.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        query(false);
      }
    });
    south = new BaseXBack(GUIConstants.FILL.NONE);
    south.setLayout(new BorderLayout(8, 8));
    initPanel();
  }

  @Override
  public void init() {
    main.add(area, BorderLayout.CENTER);
    main.add(south, BorderLayout.SOUTH);
    area.setText(Token.token(last));
    area.setCursor(0);
    area.setFont(GUIConstants.mfont);
    refresh();
  }

  @Override
  public void finish() { }

  /**
   * Initializes the components.
   */
  void initPanel() {
    final Box box = new Box(BoxLayout.X_AXIS);

    info = new BaseXLabel("");
    info.setFont(info.getFont().deriveFont((float) 13));
    info.setIcon(GUI.icon(IMGERROR));
    info.setName(Integer.toString(0));
    info.setCursor(GUIConstants.CURSORHAND);
    info.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        area.setCursor(Integer.parseInt(info.getName()));
        area.requestFocusInWindow();
      }
    });
    BaseXLayout.enable(info, false);
    south.add(info, BorderLayout.CENTER);

    filter = new BaseXButton(BUTTONFILTER, HELPFILTER, null);
    filter.addKeyListener(main);
    filter.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        View.notifyContext(GUI.context.marked(), false);
      }
    });
    box.add(filter);
    
    south.add(box, BorderLayout.EAST);
  }

  @Override
  void refresh() {
    final Nodes nodes = GUI.context.marked();
    final boolean marked = nodes != null && nodes.size != 0;
    BaseXLayout.enable(filter, !GUIProp.filterrt && marked);
  }

  @Override
  void query(final boolean force) {
    final String text = Token.string(area.getText());
    if(force || !text.equals(last)) {
      last = text;
      GUI.get().execute(Commands.XQUERY,
          text.trim().length() == 0 ? "." : text);
    }
  }

  /**
   * Sets a new XQuery request.
   * @param xq XQuery
   */
  public void setXQuery(final String xq) {
    if(!xq.equals(last)) {
      last = xq;
      GUI.get().execute(Commands.XQUERY, xq.trim().length() == 0 ? "." : xq);
    }
  }

  @Override
  void quit() {
  }

  @Override
  void info(final String inf, final boolean ok) {
    final String text = ok ? "" : inf.replaceAll("Stopped.*", "");
    info.setText(text);
    info.setToolTipText(ok ? null : text);
    BaseXLayout.enable(info, !ok);

    err = -1;
    if(!ok) {
      final Matcher m = ERRPATTERN.matcher(inf);
      int el = 0;
      int ec = 0;
      if(m.matches()) {
        el = Integer.parseInt(m.group(1));
        ec = Integer.parseInt(m.group(2));
      }
      int l = 1;
      int c = 1;
      final int ll = last.length();
      for(int i = 0; i < ll; c++, i++) {
        if(l == el && c == ec) {
          err = i;
          break;
        }
        if(last.charAt(i) < ' ') {
          l++;
          c = 0;
        }
      }
    }
    
    area.error(-1);
    info.setName(Integer.toString(err));
    error.sleep(500);
  }
  
  /** Last error position. */
  protected int err;
  
  /** Delays the display of error information. */
  protected Action error = new Action() {
    @Override
    public void action() {
      area.error(err);
    }
  };
}
