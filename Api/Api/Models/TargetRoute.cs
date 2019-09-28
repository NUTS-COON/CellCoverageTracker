using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;

namespace Api.Models
{
    public class TargetRoute
    {
        public int TravelTime { get; set; }
        public IEnumerable<TransportRoute> Routes { get; set; }

        public RoutePoint GetStartPoint() => Routes.FirstOrDefault().Points.FirstOrDefault();
        public RoutePoint GetLastPoint() => Routes.LastOrDefault().Points.LastOrDefault();
    }

    public class TransportRoute
    {
        public string Transport { get; set; }
        public List<RoutePoint> Points { get; set; }
    }

    public class RoutePoint
    {
        public Coordinate Coordinate { get; set; }
        public string Description { get; set; }
        public string Time { get; set; }

        public TimeSpan GetTime() => DateTime.ParseExact(Time, "HH:mm:ss", CultureInfo.InvariantCulture).TimeOfDay;
    }
}
