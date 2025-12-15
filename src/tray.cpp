#include "tray.hpp"
#include <gtk/gtk.h>
#include <libayatana-appindicator/app-indicator.h>

static QuitCallback g_quit_callback = nullptr;

static void on_quit_menu_activate() {
  if (g_quit_callback) {
    g_quit_callback();
  }
}

void setupTray(const std::string &statusLabel, QuitCallback onQuit) {
  g_quit_callback = onQuit;

  AppIndicator *indicator = app_indicator_new(
      "copas", "edit-paste", APP_INDICATOR_CATEGORY_APPLICATION_STATUS);
  app_indicator_set_status(indicator, APP_INDICATOR_STATUS_ACTIVE);

  GtkWidget *menu = gtk_menu_new();

  GtkWidget *labelItem = gtk_menu_item_new_with_label(statusLabel.c_str());
  gtk_widget_set_sensitive(labelItem, false);
  gtk_menu_shell_append(GTK_MENU_SHELL(menu), labelItem);

  gtk_menu_shell_append(GTK_MENU_SHELL(menu), gtk_separator_menu_item_new());

  GtkWidget *quitItem = gtk_menu_item_new_with_label("Quit");
  g_signal_connect(quitItem, "activate", G_CALLBACK(on_quit_menu_activate),
                   nullptr);
  gtk_menu_shell_append(GTK_MENU_SHELL(menu), quitItem);

  gtk_widget_show_all(menu);
  app_indicator_set_menu(indicator, GTK_MENU(menu));
}
