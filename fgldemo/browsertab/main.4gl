#+ little demo program exploring the plugins capabilities
CONSTANT BROWSERTAB = "BrowserTab"
MAIN
  MENU
    COMMAND "Show Url"
      CALL showURL("https://www.4js.com")
    COMMAND "ShowN'Close" "Tests closing from the Genero code"
      CALL showURL("https://www.4js.com")
      MENU
        ON TIMER 5
          MESSAGE "before closeURL"
          CALL closeURL()
          EXIT MENU
      END MENU
    COMMAND "isAvailable"
      CALL isAvailable()
    COMMAND "raiseErr"
      CALL raiseErr()
    COMMAND "customTabBrowsers"
      CALL customTabBrowsers()
    COMMAND "Exit"
      EXIT MENU
  END MENU
END MAIN

FUNCTION showURL(url STRING)
  DEFINE ret STRING
  TRY
    CALL ui.Interface.frontCall(
        "cordova", "call", [BROWSERTAB, "openUrl", url], [ret])
    MESSAGE "ret:", ret
  CATCH
    ERROR err_get(status)
  END TRY
END FUNCTION

FUNCTION closeURL()
  DEFINE ret STRING
  TRY
    CALL ui.Interface.frontCall("cordova", "call", [BROWSERTAB, "close"], [ret])
    MESSAGE "closeUrl ret:", ret
  CATCH
    ERROR err_get(status)
  END TRY
END FUNCTION

FUNCTION isAvailable()
  DEFINE ret STRING
  TRY
    CALL ui.Interface.frontCall(
        "cordova", "call", [BROWSERTAB, "isAvailable"], [ret])
    MESSAGE "ret3:", ret
  CATCH
    ERROR err_get(status)
  END TRY
END FUNCTION

FUNCTION raiseErr()
  DEFINE ret STRING
  TRY
    CALL ui.Interface.frontCall(
        "cordova", "call", [BROWSERTAB, "err"], [ret])
    MESSAGE "ret4:", ret
  CATCH
    ERROR err_get(status)
  END TRY
END FUNCTION

FUNCTION customTabBrowsers()
  DEFINE ret STRING
  TRY
    CALL ui.Interface.frontCall(
        "cordova", "call", [BROWSERTAB, "customTabBrowsers"], [ret])
    MESSAGE "ret5:", ret
  CATCH
    ERROR err_get(status)
  END TRY
END FUNCTION
