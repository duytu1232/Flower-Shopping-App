//package com.example.flowerapp.Adapters;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.flowerapp.Models.Order;
//import com.example.flowerapp.R;
//
//import java.util.List;
//
//public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
//    private List<Order> orderList;
//
//    public OrderAdapter(List<Order> orderList) {
//        this.orderList = orderList;
//    }
//
//    @NonNull
//    @Override
//    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
//        return new OrderViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
//        Order order = orderList.get(position);
//        holder.orderTitle.setText(order.getTitle());
//        holder.orderStatus.setText(order.getStatus());
//        holder.orderDate.setText(order.getDate());
//        holder.orderImage.setImageResource(order.getImageResId());
//    }
//
//    @Override
//    public int getItemCount() {
//        return orderList.size();
//    }
//
//    public static class OrderViewHolder extends RecyclerView.ViewHolder {
//        TextView orderTitle, orderStatus, orderDate;
//        ImageView orderImage;
//
//        public OrderViewHolder(@NonNull View itemView) {
//            super(itemView);
//            orderTitle = itemView.findViewById(R.id.orderTitle);
//            orderStatus = itemView.findViewById(R.id.orderStatus);
//            orderDate = itemView.findViewById(R.id.orderDate);
//            orderImage = itemView.findViewById(R.id.orderImage);
//        }
//    }
//}
