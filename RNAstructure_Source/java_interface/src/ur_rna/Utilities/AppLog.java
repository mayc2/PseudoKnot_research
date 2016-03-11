package ur_rna.Utilities;

import java.io.PrintStream;

/**
 * Created by Richard
 */
public class AppLog {
    protected static PrintStream nullStream = new NullPrintStream();
    public static Verbosity DefaultLogImportance = Verbosity.Info;
    private static AppLog defaultAppLog;
    public static AppLog getDefault() {
        if (defaultAppLog == null)
            defaultAppLog = new AppLog();
        return defaultAppLog;
    }
    public static void setDefault(AppLog defLog) {
        defaultAppLog = defLog;
    }

    {
        setDebug(Convert.ToBool(System.getProperty("debug")));
        setVerbosity(Convert.ToInt(System.getProperty("verbose"), 1));
    }

    public boolean isTraceEnabled() { return Verbosity.Trace.importance <= _requiredVerbosity.importance; }
    public boolean isDebugEnabled() { return Verbosity.Debug.importance <= _requiredVerbosity.importance; }

    public boolean isEnabled(final Verbosity verbosity) {
        return verbosity.importance <= _requiredVerbosity.importance;
    }

    protected boolean _debug = false;
    protected Verbosity _requiredVerbosity = Verbosity.Info;


//    /* Use of the following stream references allows the application to easily redirect. */
    private PrintStream[] streams = new PrintStream[] {
        new NullPrintStream(), System.err,  System.err, System.out, System.out, System.out
    };

    public boolean getDebug() { return _debug; }
    public void setDebug(boolean value) { _debug = value; }

    public Verbosity getVerbosity() { return _requiredVerbosity; }
    public void setVerbosity(int value) throws IllegalArgumentException {
        _requiredVerbosity = Verbosity.fromImportance(value);
    }
    public void setVerbosity(Verbosity value) throws IllegalArgumentException {
        getStream(value); //throws error if invalid.
        _requiredVerbosity = value;
    }

    public void Log(String s) {
        Log(s, DefaultLogImportance);
    }
    public void Log(String format, Object ... args) {
        LogFmt(format, DefaultLogImportance, args);
    }
    private void LogFmt(String format, Verbosity importance, Object ... args) {
        Log(String.format(format, args), importance);
    }
    public void Log(String s, Verbosity messageVerbosity) {
        if (messageVerbosity.importance <= _requiredVerbosity.importance || _debug && messageVerbosity == Verbosity.Debug)
            try {
                getStream(messageVerbosity).println(s);
            } catch (IllegalArgumentException e) {
                e.printStackTrace(System.err);
                System.err.println(s);
            }
    }

    public PrintStream getStream(Verbosity streamVerbosity) throws IllegalArgumentException {
        int i = streamVerbosity.importance;
        if (i > _requiredVerbosity.importance)
            return nullStream;
        if (i < 0)
            throw new IllegalArgumentException("Invalid log type: " + i);
        return streams[streamVerbosity.importance];
    }

    public PrintStream getDbgStream() { return getStream(Verbosity.Debug); }
    public PrintStream getErrStream() { return getStream(Verbosity.Error); }
    public PrintStream getTrStream() { return getStream(Verbosity.Trace); }

    public void trace(String s) { Log(s, Verbosity.Trace); }
    public void debug(String s) { Log(s, Verbosity.Debug); }
    public void info(String s) { Log(s, Verbosity.Info); }
    public void warn(String s) { Log(s, Verbosity.Warn); }
    public void error(String s) { Log(s, Verbosity.Error); }

    public void trace(String format, Object... args) { LogFmt(format, Verbosity.Trace, args); }
    public void debug(String format, Object... args) { LogFmt(format, Verbosity.Debug, args); }
    public void info(String format, Object... args) { LogFmt(format, Verbosity.Info, args); }
    public void warn(String format, Object... args) {        LogFmt(format, Verbosity.Warn, args);    }
    public void error(String format, Object... args) { LogFmt(format, Verbosity.Error, args); }
    public void error(String s, Throwable e) { Log(s, Verbosity.Error); Log(getErrorInfo(e), Verbosity.Error); }

    public static String getErrorInfo(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
}
