package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.xml.XMLInput;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Data.Type;
import org.basex.io.IO;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.IndexAccess;
import org.basex.query.ft.FTIndexAccess;
import org.basex.query.ft.FTWords;
import org.basex.query.item.DBNode;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.TokenBuilder;

/**
 * Project specific functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FNBaseX extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case EVAL:  return eval(ctx);
      case INDEX: return index(ctx);
      default:    return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case READ:   return text(ctx);
      case RANDOM: return random();
      case DB:     return db(ctx);
      default:     return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    return func == FunDef.DB && expr[0].i() &&
      (expr.length == 1 || expr[1].i()) ? db(ctx) : this;
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter eval(final QueryContext ctx) throws QueryException {
    final QueryContext qt = new QueryContext(ctx.context);
    qt.parse(string(checkStr(expr[0], ctx)));
    qt.compile();
    return qt.iter();
  }

  /**
   * Performs the text function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item text(final QueryContext ctx) throws QueryException {
    final byte[] name = checkStr(expr[0], ctx);
    final IO io = IO.get(string(name));
    if(!Prop.web && io.exists()) {
      try {
        final XMLInput in = new XMLInput(io);
        final int len = (int) in.length();
        final TokenBuilder tb = new TokenBuilder(len);
        while(in.pos() < len) tb.addUTF(in.next());
        in.finish();
        return Str.get(tb.finish());
      } catch(final IOException ex) {
        Main.debug(ex);
      }
    }
    Err.or(NOFILE, name);
    return null;
  }

  /**
   * Performs the db function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item db(final QueryContext ctx) throws QueryException {
    DBNode node = ctx.doc(checkStr(expr[0], ctx), false, true);

    if(expr.length == 2) {
      final Item it = expr[1].atomic(ctx);
      if(it == null || !it.u() && !it.n()) Err.num(info(), it);
      final long pre = it.itr();
      if(pre < 0 || pre >= node.data.meta.size) Err.or(NOPRE, pre);
      node = new DBNode(node.data, (int) pre);
    }
    return node;
  }

  /**
   * Performs the random function.
   * @return iterator
   */
  private Item random() {
    return Dbl.get(Math.random());
  }

  /**
   * Performs the contains lower case function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter index(final QueryContext ctx) throws QueryException {
    final Data data = ctx.data();
    if(data == null) Err.or(XPNOCTX, this);

    final IndexContext ic = new IndexContext(ctx, data, null, true);
    final String type = string(checkStr(expr[1], ctx)).toLowerCase();
    final byte[] word = checkStr(expr[0], ctx);

    if(type.equals(FULLTEXT)) {
      if(!data.meta.ftxindex) Err.or(NOIDX, FULLTEXT);
      return new FTIndexAccess(new FTWords(
          data, word, ctx.ftpos == null), ic).iter(ctx);
    }
    if(type.equals(TEXT)) {
      if(!data.meta.txtindex) Err.or(NOIDX, TEXT);
      return new IndexAccess(expr[0], Type.TXT, ic).iter(ctx);
    }
    if(type.equals(ATTRIBUTE)) {
      if(!data.meta.atvindex) Err.or(NOIDX, ATTRIBUTE);
      return new IndexAccess(expr[0], Type.ATV, ic).iter(ctx);
    }

    Err.or(WHICHIDX, type);
    return null;
  }
}
