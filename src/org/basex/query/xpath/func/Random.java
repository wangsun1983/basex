package org.basex.query.xpath.func;

import static org.basex.Text.*;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Nod;
import org.basex.util.IntList;

/**
 * Constructor for the implementation specific random() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Random extends Func {
  /** Name of function. */
  public static final String NAME = NAMESPACE + ":random";

  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Random(final Expr[] arg) {
    super(arg, NAME + "(nodeset)");
  }

  @Override
  public Item eval(final XPContext ctx) 
      throws QueryException {
    
    final Nod ns = (Nod) evalArgs(ctx)[0];
    if(ns.size < 2) return ns;

    final IntList list = new IntList();
    final java.util.Random rnd = new java.util.Random();
    final int[] n = ns.nodes;
    int c = n.length;
    while(c > 0) {
      final int r = rnd.nextInt(n.length);
      if(n[r] != -1) {
        list.add(n[r]);
        n[r] = -1;
        c--;
      }
    }
    return new Nod(list.finish(), ctx);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1 && args[0].returnedValue() == Nod.class;
  }
}
