package org.basex.data;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.fuse.DeepFS;
import org.basex.index.Index;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.index.Names;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * This class provides access to the database. The storage
 * representation depends on the underlying implementation.
 * Note that the methods of this class are optimized for performance.
 * They will not check if you request wrong data. If you ask for a text
 * node, e.g., get sure your pre value actually points to a text node.
 * The same applies to the update operations; if you write an attribute
 * to an element node, your database will get messed up.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Data {
  /** Node kind: Document. */
  public static final byte DOC = 0x00;
  /** Node kind: Element. */
  public static final byte ELEM = 0x01;
  /** Node kind: Text. */
  public static final byte TEXT = 0x02;
  /** Node kind: Attribute. */
  public static final byte ATTR = 0x03;
  /** Node kind: Comment. */
  public static final byte COMM = 0x04;
  /** Node kind: Processing instruction. */
  public static final byte PI = 0x05;

  /** Index types. */
  public enum Type {
    /** Attribute index. */ ATN,
    /** Tag index.       */ TAG,
    /** Text index.      */ TXT,
    /** Attribute index. */ ATV,
    /** Full-text index. */ FTX,
  };

  /** Meta data. */
  public MetaData meta;
  /** Tag index. */
  public Names tags;
  /** Attribute name index. */
  public Names atts;
  /** Namespace index. */
  public Namespaces ns;
  /** Path Summary. */
  public PathSummary path;

  /** Text index. */
  protected Index txtindex;
  /** Attribute value index. */
  protected Index atvindex;
  /** Full-text index instance. */
  protected Index ftxindex;

  /** File system indicator. */
  public DeepFS fs;
  /** Index Reference for name tag. */
  public int nameID;
  /** Index References. */
  public int sizeID;

  /**
   * Dissolves the references to often used tag names and attributes.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void init() throws IOException {
    if(tags.id(DataText.DEEPFS) != 0) fs = new DeepFS(this);
    nameID = atts.id(DataText.NAME);
    sizeID = atts.id(DataText.SIZE);
  }

  /**
   * Closes the current database.
   * @throws IOException I/O exception
   */
  public final void close() throws IOException {
    if(fs != null) fs.close();
    cls();
  }

  /**
   * Internal method to close the database.
   * @throws IOException I/O exception
   */
  protected abstract void cls() throws IOException;

  /**
   * Flushes the table data.
   */
  public abstract void flush();

  /**
   * Closes the specified index.
   * @param index index to be closed
   * @throws IOException I/O exception
   */
  public abstract void closeIndex(Type index) throws IOException;

  /**
   * Assigns the specified index.
   * @param type index to be opened
   * @param ind index instance
   */
  public abstract void setIndex(Type type, Index ind);

  /**
   * Returns a unique node id.
   * @param pre pre value
   * @return node id
   */
  public abstract int id(int pre);

  /**
   * Returns a pre value.
   * @param id unique node id
   * @return pre value or -1 if id was not found
   */
  public abstract int pre(int id);

  /**
   * Returns a node kind.
   * @param pre pre value
   * @return node kind
   */
  public abstract int kind(int pre);

  /**
   * Returns a pre value of the parent node.
   * @param pre pre value
   * @param kind node kind
   * @return pre value of the parent node
   */
  public abstract int parent(int pre, int kind);

  /**
   * Returns a size value (number of descendant table entries).
   * @param pre pre value
   * @param kind node kind
   * @return size value
   */
  public abstract int size(int pre, int kind);

  /**
   * Returns a tag id (reference to the tag index).
   * @param pre pre value
   * @return token reference
   */
  public abstract int tagID(int pre);

  /**
   * Returns an id for the specified tag.
   * @param tok token to be found
   * @return name reference
   */
  public final int tagID(final byte[] tok) {
    return tags.id(tok);
  }

  /**
   * Returns a tag name.
   * @param pre pre value
   * @return name reference
   */
  public final byte[] tag(final int pre) {
    return tags.key(tagID(pre));
  }

  /**
   * Returns a tag namespace (reference to the tag namespace).
   * @param pre pre value
   * @return token reference
   */
  public abstract int tagNS(int pre);

  /**
   * Returns namespace key and value ids.
   * @param pre pre value
   * @return key and value ids
   */
  public abstract int[] ns(int pre);

  /**
   * Returns a text.
   * @param pre pre value
   * @return atomized value
   */
  public abstract byte[] text(int pre);

  /**
   * Returns a text as double value.
   * @param pre pre value
   * @return numeric value
   */
  public abstract double textNum(int pre);

  /**
   * Returns a text length.
   * @param pre pre value
   * @return length
   */
  public abstract int textLen(int pre);

  /**
   * Returns an attribute name.
   * @param pre pre value
   * @return name reference
   */
  public final byte[] attName(final int pre) {
    return atts.key(attNameID(pre));
  }

  /**
   * Returns an attribute name id (reference to the attribute name index).
   * @param pre pre value
   * @return token reference
   */
  public abstract int attNameID(int pre);

  /**
   * Returns an id for the specified attribute name.
   * @param tok token to be found
   * @return name reference
   */
  public final int attNameID(final byte[] tok) {
    return atts.id(tok);
  }

  /**
   * Returns an attribute namespace (reference to the attribute namespace).
   * @param pre pre value
   * @return token reference
   */
  public abstract int attNS(int pre);

  /**
   * Returns an attribute value.
   * @param pre pre value
   * @return atomized value
   */
  public abstract byte[] attValue(int pre);

  /**
   * Returns an attribute value length.
   * @param pre pre value
   * @return length
   */
  public abstract int attLen(int pre);

  /**
   * Returns an attribute value as double value.
   * @param pre pre value
   * @return numeric value
   */
  public abstract double attNum(int pre);

  /**
   * Finds the specified attribute and returns its value.
   * @param att attribute to be found
   * @param pre pre value
   * @return attribute value
   */
  public final byte[] attValue(final int att, final int pre) {
    final int a = pre + attSize(pre, kind(pre));
    int p = pre;
    while(++p != a) if(attNameID(p) == att) return attValue(p);
    return null;
  }

  /**
   * Returns a number of attributes.
   * @param pre pre value
   * @param kind node kind
   * @return number of attributes
   */
  public abstract int attSize(int pre, int kind);

  /**
   * Returns the indexed id references for the specified token.
   * @param token index token reference
   * @return id array
   */
  public final IndexIterator ids(final IndexToken token) {
    if(token.get().length > MAXLEN) return null;
    switch(token.type()) {
      case TXT: return txtindex.ids(token);
      case ATV: return atvindex.ids(token);
      case FTX: return ftxindex.ids(token);
      default:  return null;
    }
  }

  /**
   * Returns the number of indexed id references for the specified token.
   * @param token text to be found
   * @return id array
   */
  public final int nrIDs(final IndexToken token) {
    // token to long.. no results can be expected
    if(token.get().length > MAXLEN) return Integer.MAX_VALUE;
    switch(token.type()) {
      case TXT: return txtindex.nrIDs(token);
      case ATV: return atvindex.nrIDs(token);
      case FTX: return ftxindex.nrIDs(token);
      default:  return Integer.MAX_VALUE;
    }
  }

  /**
   * Returns info on the specified index structure.
   * @param type index type
   * @return info
   */
  public final byte[] info(final Type type) {
    switch(type) {
      case TAG: return tags.info();
      case ATN: return atts.info();
      case TXT: return txtindex.info();
      case ATV: return atvindex.info();
      case FTX: return ftxindex.info();
      default: return EMPTY;
    }
  }

  /**
   * Returns the document nodes.
   * @return root nodes
   */
  public final int[] doc() {
    final IntList il = new IntList();
    for(int i = 0; i < meta.size; i += size(i, Data.DOC)) il.add(i);
    return il.finish();
  }

  /**
   * Returns an atomized content for any node kind.
   * The atomized value can be an attribute value or XML content.
   * @param pre pre value
   * @return atomized value
   */
  public final byte[] atom(final int pre) {
    switch(kind(pre)) {
      case TEXT: case COMM:
        return text(pre);
      case ATTR:
        return attValue(pre);
      case PI:
        final byte[] txt = text(pre);
        return substring(txt, indexOf(txt, ' ') + 1);
      default:
        return atm(pre);
    }
  }

  /**
   * Atomizes content of the specified pre value.
   * @param pre pre value
   * @return atomized value
   */
  private byte[] atm(final int pre) {
    // create atomized text node
    final TokenBuilder tb = new TokenBuilder();
    int p = pre;
    final int s = p + size(p, kind(p));
    while(p != s) {
      final int k = kind(p);
      if(k == TEXT) tb.add(text(p));
      p += attSize(p, k);
    }
    return tb.finish();
  }

  /**
   * Updates a tag name, text node, comment or processing instruction.
   * @param pre pre of the text node to change
   * @param val value to be updated
   */
  public abstract void update(int pre, byte[] val);

  /**
   * Updates an attribute name and value.
   * @param pre pre of node to insert after
   * @param name attribute name
   * @param val attribute value
   */
  public abstract void update(int pre, byte[] name, byte[] val);

  /**
   * Deletes a node and its descendants.
   * @param pre pre value of the node to delete
   */
  public abstract void delete(final int pre);

  /**
   * Inserts a tag name, text node, comment or processing instruction.
   * @param pre pre value
   * @param par parent of node
   * @param val value to be inserted
   * @param kind node kind
   */
  public abstract void insert(int pre, int par, byte[] val, int kind);

  /**
   * Inserts an attribute.
   * @param pre pre value
   * @param par parent of node
   * @param name attribute name
   * @param val attribute value
   */
  public abstract void insert(int pre, int par, byte[] name, byte[] val);

  /**
   * Inserts a data instance at the specified pre value.
   * Note that the specified data instance must differ from this instance.
   * @param pre value at which to insert new data
   * @param par parent pre value of node
   * @param d data instance to copy from
   */
  public abstract void insert(int pre, int par, Data d);

  /**
   * Returns the locking situation of the data.
   * @return locked flag
   */
  public abstract int getLock();

  /**
   * Locks and unlocks the data reference.
   * @param l int kind of lock
   *        1 = read lock
   *        2 = write lock
   */
  public abstract void setLock(final int l);

  @Override
  public String toString() {
    return BaseX.name(this) + "[" + meta.name + "]";
  }
}
