package org.basex.query.xpath.func;

import static org.basex.Text.*;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.item.Nod;
import org.basex.util.IntList;

/**
 * Constructor for the implementation specific sort() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Sort extends Func {
  /** Name of function. */
  public static final String NAME = NAMESPACE + ":sort";

  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Sort(final Expr[] arg) {
    super(arg, NAME + "(nodeset)");
  }

  @Override
  public Item eval(final XPContext ctx) 
      throws QueryException {
    
    final Nod ns = (Nod) evalArgs(ctx)[0];
    if(ns.size < 2) return ns;

    final int[] n = ns.nodes;
    final byte[][] val = new byte[n.length][];
    for(int i = 0; i < val.length; i++) val[i] = ctx.item.data.atom(n[i]);

    final IntList list = new IntList(n);
    list.sort(val, false, true);
    return new Nod(n, ctx);
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1 && args[0].returnedValue() == Nod.class;
  }
}
