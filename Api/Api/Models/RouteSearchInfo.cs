using System;

namespace Api.Models
{
    public class RouteSearchInfo
    {
        public DateTime? Time { get; set; }
        public Coordinate From { get; set; }
        public Coordinate To { get; set; }
    }
}
