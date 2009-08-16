package org.basex.core.proc;

import org.basex.BaseX;
import org.basex.data.Nodes;

/**
 * Evaluates the 'cs' command and sets a new initial context set.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Cs extends AQuery {
  /**
   * Default constructor.
   * @param query query
   */
  public Cs(final String query) {
    super(DATAREF, query);
  }

  @Override
  protected boolean exec() {
    final Nodes nodes = query(args[0], null);
    if(nodes == null) return false;

    if(nodes.size() != 0) {
      context.current(nodes);
      result = nodes;
    }
    return true;
  }

  @Override
  public String toString() {
    return BaseX.name(this).toUpperCase() + ' ' + args[0];
  }
}
