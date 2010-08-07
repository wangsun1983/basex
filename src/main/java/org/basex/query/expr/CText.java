package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Text fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CText extends CFrag {
  /**
   * Constructor.
   * @param ii input info
   * @param t text
   */
  public CText(final InputInfo ii, final Expr t) {
    super(ii, t);
  }

  @Override
  public FTxt atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Iter iter = ctx.iter(expr[0]);
    Item it = iter.next();
    if(it == null) return null;

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    do {
      if(more) tb.add(' ');
      tb.add(it.atom());
      more = true;
    } while((it = iter.next()) != null);

    return new FTxt(tb.finish(), null);
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.NOD_ZO;
  }

  @Override
  public String desc() {
    return info(QueryTokens.TEXT);
  }

  @Override
  public String toString() {
    return toString(Type.TXT.name);
  }
}
