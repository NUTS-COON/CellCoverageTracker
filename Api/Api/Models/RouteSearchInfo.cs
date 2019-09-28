using System;

namespace Api.Models
{
    public class RouteSearchInfo
    {
        public DateTime? Time { get; set; }
        public CoordinateOrLocation From { get; set; }
        public CoordinateOrLocation To { get; set; }
    }

    public class CoordinateOrLocation
    {
        public Coordinate Coordinate { get; set; }
        public string LocationId { get; set; }
    }
}
