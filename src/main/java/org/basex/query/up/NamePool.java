package org.basex.query.up;

import static org.basex.util.Token.*;
import java.util.Arrays;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.util.Atts;

/**
 * This class serves as a container for updated names.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class NamePool {
  /** Names. */
  private QNm[] names = new QNm[1];
  /** Attribute/element flag. */
  private boolean[] attr = new boolean[1];
  /** Number of occurrences. */
  private int[] occ = new int[1];
  /** Number of entries. */
  private int size;

  /**
   * Adds an entry to the pool.
   * @param name name
   * @param type node type
   */
  public void add(final QNm name, final Type type) {
    if(type != Type.ATT && type != Type.ELM) return;
    final int i = index(name, type == Type.ATT);
    occ[i]++;
  }

  /**
   * Removes an entry from the pool.
   * @param nod node
   */
  public void remove(final Nod nod) {
    if(nod.type != Type.ATT && nod.type != Type.ELM) return;
    final int i = index(nod.qname(), nod.type == Type.ATT);
    occ[i]--;
  }

  /**
   * Finds duplicate attributes.
   * @return duplicate attribute
   */
  QNm duplicate() {
    for(int i = 0; i < size; i++) if(occ[i] > 1) return names[i];
    return null;
  }

  /**
   * Checks if no namespace conflicts occur.
   * @return success flag
   */
  boolean nsOK() {
    final Atts at = new Atts();
    for(int i = 0; i < size; i++) {
      if(occ[i] <= 0) continue;
      final QNm nm = names[i];
      final byte[] pref = nm.pref();
      final byte[] uri = nm.uri.str();
      final int ai = at.get(pref);
      if(ai == -1) at.add(pref, uri);
      else if(!eq(uri, at.val[ai])) return false;
    }
    return true;
  }

  /**
   * Returns an index to an existing entry, or -1.
   * @param name name to be found
   * @param at attribute/element flag
   * @return index offset
   */
  private int index(final QNm name, final boolean at) {
    for(int i = 0; i < size; i++) {
      if(names[i].eq(name) && attr[i] == at) return i;
    }
    if(size == names.length) {
      names = Arrays.copyOf(names, size << 1);
      attr = Arrays.copyOf(attr, size << 1);
      occ = Arrays.copyOf(occ, size << 1);
    }
    names[size] = name;
    attr[size] = at;
    return size++;
  }
}
