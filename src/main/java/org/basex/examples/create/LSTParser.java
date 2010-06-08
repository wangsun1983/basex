package org.basex.examples.create;

import static org.basex.util.Token.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.Main;
import org.deepfs.fs.DeepFS;

/**
 * This class parses files in the LST format
 * and sends events to the specified database builder.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class LSTParser extends Parser {
  /** Date Format. */
  private static final SimpleDateFormat DATE =
    new SimpleDateFormat("yyyy.MM.dd hh:mm.ss");

  /**
   * Constructor.
   * @param path file path
   */
  public LSTParser(final String path) {
    super(path);
  }
  
  @Override
  public void parse(final Builder b) throws IOException {
    b.startDoc(token(file.name()));
    b.startElem(DeepFS.FSML, atts.reset());

    final BufferedReader br = new BufferedReader(new FileReader(file.path()));
    String line = br.readLine().replace('\\', '/');
    atts.add(DeepFS.BACKINGSTORE, token(line));
    b.startElem(DeepFS.DEEPFS, atts);

    String[] old = {};
    while(true) {
      line = br.readLine();
      if(line == null) break;
      line = line.replace('\\', '/');
      
      final String[] entries = line.split("\\t");
      String name = entries[0];

      byte[] mtime = {};
      try {
        mtime = token(DATE.parse(entries[2] + " " + entries[3]).getTime());
      } catch(final ParseException ex) {
        Main.debug(ex);
      }
      
      if(name.indexOf('/') != -1) {
        // Directory
        name = name.replaceAll("/$", "");
        final String[] path = name.split("/");

        int i = -1;
        while(++i < Math.min(old.length, path.length)) {
          if(!old[i].equals(path[i])) break;
        }
        for(int j = i; j < old.length; j++) {
          b.endElem(DeepFS.DIR);
        }
        for(int j = i; j < path.length; j++) {
          atts.reset();
          atts.add(DeepFS.NAME, token(path[i]));
          atts.add(DeepFS.MTIME, mtime);
          b.startElem(DeepFS.DIR, atts);
        }
        old = path;
      } else {
        // File
        atts.reset();
        atts.add(DeepFS.NAME, token(name));
        atts.add(DeepFS.SIZE, token(entries[1]));
        atts.add(DeepFS.MTIME, mtime);
        b.emptyElem(DeepFS.FILE, atts);
      }
    }
    br.close();
    for(int j = old.length; j > 0; j--) b.endElem(DeepFS.DIR);

    b.endElem(DeepFS.DEEPFS);
    b.endElem(DeepFS.FSML);
    b.endDoc();
    b.meta.deepfs = true;
  }
}
