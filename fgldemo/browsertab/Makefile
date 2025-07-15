BUNDLE_IDENTIFIER   = com.fourjs.browsertab
APPNAME             = BrowserTab
DEVICE              = simulator
MODE                = debug
VERBOSE             = no
IDENTITY='Iphone Dev'
PROVISIONING_PROFILE=$(HOME)/Library/MobileDevice/Provisioning\ Profiles/4js_wildcard
FORMS   = $(patsubst %.per, %.42f, $(wildcard *.per))

PLUGIN=cordova-plugin-browsertab
ifneq ($(wildcard ../../fgldemo),)
# in case you build the demo when having the plugin cloned out of github 
# the following applies
  PLUGIN=$(CURDIR)/../..
  ABSPATH=$(abspath .)
  DEMODIR:=$(dir $(ABSPATH))
  PLUGINDIR:=$(dir $(realpath $(DEMODIR)))
  GMAPLUGIN:=$(notdir $(realpath $(PLUGINDIR)))
else
  GMAPLUGIN=$(PLUGIN)
endif

MODULES  = $(patsubst %.4gl, %.42m, $(wildcard *.4gl))
FORMS    = $(patsubst %.per, %.42f, $(wildcard *.per))

%.42m: %.4gl
#	FGLLDPATH=$(FGLLDPATH) fglcomp -M --resolve-calls $*
	fglcomp -M -Wall --resolve-calls $*

%.42f: %.per
	fglform -M $*

all:: $(MODULES) $(FORMS)

run: main.42m
#	FGLLDPATH=$(FGLLDPATH) fglrun main
	FGLLDPATH=$(FGLLDPATH) fglrun main

define gmibuild
	gmibuildtool --install=$(1) --verbose=$(VERBOSE) --app-name=$(APPNAME) --bundle-id=$(BUNDLE_IDENTIFIER) --provisioning=$(PROVISIONING_PROFILE) --certificate=$(IDENTITY) --device=$(DEVICE) --mode=$(MODE) --build-cordova=$(PLUGIN)
endef

gmi.build: all
	$(call gmibuild,no)

gmi.install: all
	$(call gmibuild,yes)

gma.build: all
ifneq ($(wildcard ../../fgldemo),)
#add to scaffold the parent dir
	rm -rf project
	gmabuildtool scaffold --install-plugins ../../../$(GMAPLUGIN)
endif
	gmabuildtool build -map main.42m --build-quietly --build-cordova $(GMAPLUGIN) --build-app-name $(APPNAME) --build-app-package-name $(BUNDLE_IDENTIFIER)

#ADB_DEBUG=-d
gma.install: gma.build
	adb $(ADB_DEBUG) install -r project/build/app/outputs/apk/gma/app.apk

echo:
	echo "ABSPATH=$(ABSPATH)"
	echo "DEMODIR=$(DEMODIR)"
	echo "PLUGINDIR=$(PLUGINDIR)"
	echo "GMAPLUGIN=$(GMAPLUGIN)"

clean::
	rm -f *.sch *.dbs *.42*
	rm -rf build bin distbin project
	rm -f cordovamap.txt gmi/*.a gmi/link_flags.sh *.4pwvs *.4pwdb ../../src/ios/*.a
	rm -f ../../pluginCalls.*

distclean::clean
	rm -f ./fgldir
	rm -rf gwa_dist gas_dist build
