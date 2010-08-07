package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Castable expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Castable extends Single {
  /** Instance. */
  private final SeqType seq;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param s sequence type
   */
  public Castable(final InputInfo ii, final Expr e, final SeqType s) {
    super(ii, e);
    seq = s;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return checkUp(expr, ctx).item() ? preEval(ctx) : this;
  }

  @Override
  public Bln atomic(final QueryContext ctx, final InputInfo ii) {
    try {
      final Item it = expr.atomic(ctx, input);
      seq.cast(it, this, ctx, input);
      return Bln.TRUE;
    } catch(final QueryException ex) {
      return Bln.FALSE;
    }
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.BLN;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr + " " + CASTABLE + " " + AS + " " + seq;
  }
}
