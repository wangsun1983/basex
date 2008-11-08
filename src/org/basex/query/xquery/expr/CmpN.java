package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;

/**
 * Node comparison.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CmpN extends Arr {
  /** Comparators. */
  public enum COMP {
    /** Node Comparison: same. */
    EQ("is") {
      @Override
      public boolean e(final Item a, final Item b) {
        return ((Nod) a).is((Nod) b);
      }
    },

    /** Node Comparison: before. */
    ET("<<") {
      @Override
      public boolean e(final Item a, final Item b) {
        return ((Nod) a).diff((Nod) b) < 0;
      }
    },

    /** Node Comparison: after. */
    GT(">>") {
      @Override
      public boolean e(final Item a, final Item b) {
        return ((Nod) a).diff((Nod) b) > 0;
      }
    };

    /** String representation. */
    public final String name;

    /**
     * Constructor.
     * @param n string representation
     */
    COMP(final String n) { name = n; }

    /**
     * Evaluates the expression.
     * @param a first item
     * @param b second item
     * @return result
     * @throws XQException evaluation exception
     */
    public abstract boolean e(Item a, Item b) throws XQException;

    @Override
    public String toString() { return name; }
  }

  
  /** Comparator. */
  private final COMP cmp;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @param c comparator
   */
  public CmpN(final Expr e1, final Expr e2, final COMP c) {
    super(e1, e2);
    cmp = c;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item a = ctx.atomic(expr[0], this, true);
    if(a == null) return Iter.EMPTY;
    final Item b = ctx.atomic(expr[1], this, true);
    if(b == null) return Iter.EMPTY;

    if(!a.node()) Err.type(info(), Type.NOD, a);
    if(!b.node()) Err.type(info(), Type.NOD, b);
    return Bln.get(cmp.e(a, b)).iter();
  }
  
  @Override
  public Type returned() {
    return Type.BLN;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(cmp.name), NS, timer());
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String info() {
    return "'" + cmp + "' operator";
  }

  @Override
  public String toString() {
    return toString(" " + cmp + " ");
  }
}
