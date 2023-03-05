package shray.us.physics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.colors.XChartSeriesColors;

public class Main extends JavaPlugin implements CommandExecutor, Listener {
  
  public static void main(String[] args) {}
  
  private double lastVelocity = 0.0D;
  
  private DecimalFormat dec = new DecimalFormat("0.00");
  
  double phase = 0.0D;
  
  double[][] initdata = getSineData(this.phase);
  
  ArrayList<Double> xvals = new ArrayList<Double>();
  
  ArrayList<Double> posyvals = new ArrayList<Double>();
  
  ArrayList<Double> velyvals = new ArrayList<Double>();
  
  ArrayList<Double> accyvals = new ArrayList<Double>();

  private boolean paused;
  
  private BukkitTask task;
  
  private void start() {
    final XYChart poschart = QuickChart.getChart("Y-Position (m)", "Time", "Y-Position", "position", this.initdata[0], this.initdata[1]);
    ((XYSeries)poschart.getSeriesMap().get("position")).setLineColor(XChartSeriesColors.BLUE);
    ((XYStyler)poschart.getStyler()).setLegendVisible(false);
    final SwingWrapper<XYChart> possw = new SwingWrapper((Chart)poschart);
    possw.displayChart();
    final XYChart velchart = QuickChart.getChart("Velocity (m/s)", "Time", "Velocity", "velocity", this.initdata[0], this.initdata[1]);
    ((XYSeries)velchart.getSeriesMap().get("velocity")).setLineColor(XChartSeriesColors.GREEN);
    ((XYStyler)velchart.getStyler()).setLegendVisible(false);
    final SwingWrapper<XYChart> velsw = new SwingWrapper((Chart)velchart);
    velsw.displayChart();
    final XYChart accchart = QuickChart.getChart("Acceleration (m/s, "Time", "Acceleration", "acceleration", this.initdata[0], this.initdata[1]);
    ((XYSeries)accchart.getSeriesMap().get("acceleration")).setLineColor(XChartSeriesColors.RED);
    ((XYStyler)accchart.getStyler()).setLegendVisible(false);
    final SwingWrapper<XYChart> accsw = new SwingWrapper((Chart)accchart);
    accsw.displayChart();
    this.paused = false;
    this.task = (new BukkitRunnable() {
        public void run() {
          for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            double v = onlinePlayer.getVelocity().getY();
            double a = Main.this.lastVelocity - v;
            Main.this.lastVelocity = v;
            String niceV = String.valueOf(Main.this.dec.format((int)(v * 2000.0D) / 100.0D)) + " m/s";
            String niceA = String.valueOf(Main.this.dec.format((int)(a * 2000.0D) / 100.0D)) + " m/s;
            if (Math.abs((int)(v * 2000.0D) / 100.0D) > 78.39D)
              niceV = ChatColor.BOLD + niceV; 
            if (!niceV.startsWith("-"))
              niceV = " " + niceV; 
            if (!niceA.startsWith("-"))
              niceA = " " + niceA; 
            String gap = "        ";
            for (int i = 0; i < 6 - niceV.length(); i++)
              gap = String.valueOf(gap) + " "; 
            onlinePlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent)new TextComponent(ChatColor.GREEN + niceV + gap + ChatColor.RESET + ChatColor.RED + niceA));
            Main.this.phase += 0.05D;
            Main.this.xvals.add(Double.valueOf(Main.round(Main.this.phase, 1)));
            Main.this.posyvals.add(Double.valueOf(Main.round(onlinePlayer.getLocation().getY(), 2)));
            poschart.updateXYSeries("position", Main.this.xvals, Main.this.posyvals, null);
            possw.repaintChart();
            Main.this.velyvals.add(Double.valueOf((int)(v * 2000.0D) / 100.0D));
            velchart.updateXYSeries("velocity", Main.this.xvals, Main.this.velyvals, null);
            velsw.repaintChart();
            Main.this.accyvals.add(Double.valueOf((int)(a * 2000.0D) / 100.0D));
            accchart.updateXYSeries("acceleration", Main.this.xvals, Main.this.accyvals, null);
            accsw.repaintChart();
          } 
        }
      }).runTaskTimer((Plugin)this, 0L, 1L);
  }
  
  private void stop() {
    if (this.task != null) {
      this.task.cancel();
      this.task = null;
    } 
    this.paused = false;
  }
  
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("charts")) {
      if (args.length == 1) {
        if (args[0].equalsIgnoreCase("start")) {
          if (this.task != null) {
            sender.sendMessage(ChatColor.GREEN + "Already started.");
            return false;
          } 
          start();
          sender.sendMessage(ChatColor.GREEN + "Started.");
          return true;
        } 
        if (args[0].equalsIgnoreCase("pause")) {
          this.paused = !this.paused;
          sender.sendMessage(ChatColor.GREEN + (this.paused ? "Paused." : "Resumed."));
          return true;
        } 
        if (args[0].equalsIgnoreCase("stop")) {
          stop();
          sender.sendMessage(ChatColor.RED + "Stopped.");
          return true;
        } 
      } 
      sender.sendMessage(ChatColor.RED + "Invalid usage. Please use:");
      sender.sendMessage(ChatColor.RED + "/charts start");
      sender.sendMessage(ChatColor.RED + "/charts pause");
      sender.sendMessage(ChatColor.RED + "/charts stop");
      return false;
    } 
    return false;
  }
  
  private static double[][] getSineData(double phase) {
    double[] xData = new double[100];
    double[] yData = new double[100];
    for (int i = 0; i < xData.length; i++) {
      double radians = phase + 6.283185307179586D / xData.length * i;
      xData[i] = radians;
      yData[i] = Math.sin(radians);
    } 
    return new double[][] { xData, yData };
  }
  
  private static double round(double value, int precision) {
    int scale = (int)Math.pow(10.0D, precision);
    return Math.round(value * scale) / scale;
  }
}
