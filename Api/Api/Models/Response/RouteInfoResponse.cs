using System.Collections.Generic;

namespace Api.Models
{
    public class RouteInfoResponse
    {
        public List<Coordinate> Route { get; set; }
        public List<CellPoint> Points { get; set; }
        public RouteStat Stat { get; set; }
    }

    public class RouteStat
    {
        public List<RouteOperatorStat> OperatorStats { get; set; }
        
        //public string BestOperatorName { get; set; }
    }

    public class RouteOperatorStat
    {
        public string OperatorName { get; set; }
        public decimal PercentNone { get; set; }
        public decimal Percent2G { get; set; }
        public decimal Percent3G { get; set; }
        public decimal Percent4G { get; set; }
    }
}