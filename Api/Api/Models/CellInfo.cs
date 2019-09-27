using System;

namespace Api.Models
{
    public class CellInfo
    {
        public string IMEI { get; set; }
        public string OperatorName { get; set; }
        public decimal Latitude { get; set; }
        public decimal Longitude { get; set; }
        public string CellType { get; set; }
        public int Level { get; set; }
        public DateTime Timestamp { get; set; }
    }
}